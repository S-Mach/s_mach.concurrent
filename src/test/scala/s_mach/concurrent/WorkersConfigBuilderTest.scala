/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____        __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent

import scala.concurrent.Future
import scala.util.{Random, Failure, Success}
import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.util._
import TestBuilder._

class WorkersConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "WorkerConfigBuilder.map-t0" must "should invoke futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.workers(3).map(success)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal (Success(items))
        isConcurrentSchedule(items.size, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= 0.98
  }

  "WorkerConfigBuilder.map-t1" must "should invoke at most workerCount futures concurrently" in {
    val cpuCount = Runtime.getRuntime.availableProcessors()
    require(cpuCount >= 3, s"Only $cpuCount CPUs detected. At least 3 are required to run this test.")

    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        // Always leave a spare cpu to detect if more workers are concurrent than should be
        val workerCount = 2 + Random.nextInt(cpuCount - 3)
        val lock = new Object
        var maxActiveCount = 0
        var activeCount = 0
        val result = items.workers(workerCount).map { item =>
          lock.synchronized {
            activeCount = activeCount + 1
            maxActiveCount = Math.max(maxActiveCount, activeCount)
          }
          success(item) sideEffect {
            lock.synchronized {
              activeCount = activeCount - 1
            }
          }
        }

        waitForActiveExecutionCount(0)

        result.getTry should equal (Success(items))
        maxActiveCount should be <= workerCount
        (workerCount, maxActiveCount)
      }

    // Not possible to guarantee workers will execute concurrently, but in general should be true
    val concurrentWorkerPercent = result.count { case (workerCount, maxActiveCount) =>
      maxActiveCount == workerCount || maxActiveCount == workerCount - 1
    } / result.size.toDouble
    concurrentWorkerPercent should be >= 0.85
  }

  "WorkerConfigBuilder.flatMap-t2" must "execute futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.workers(2).flatMap(successN)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
        isConcurrentSchedule(items.size, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= 0.98
  }

  "WorkerConfigBuilder.foreach-t3" must "execute futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.workers(2).foreach(success)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal(Success(()))
        val eventMap = sched.eventMap
        (1 to items.size) foreach { i =>
          eventMap.contains(s"success-$i") should equal(true)
        }
        isConcurrentSchedule(items.size, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= 0.97
  }

  "WorkerConfigBuilder.map-t4" must "complete immediately after any Future fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val latch = Latch()
      val result = items.zipWithIndex.workers(2).map { case (item, i) =>
        i match {
          case 1 => fail(item) //sideEffect latch.set()
          case _ => latch happensBefore success(item)
        }
      }

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      latch.set()

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [ConcurrentThrowable]
      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      // Note: there is a race condition between whether fail causes workers to exit before workers starts up success-3
      (4 to 6).foreach { i =>
        sched.orderedEvents.exists(_.id == s"success-$i") should equal (false)
      }
    }
  }

  "WorkerConfigBuilder.map-t5" must "complete with an exception if the last worker fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val lastIdx = items.size - 1
      val result = items.zipWithIndex.workers(2).map { case (item, i) =>
        if(lastIdx == i) {
          fail(item)
        } else {
          success(item)
        }
      }

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [ConcurrentThrowable]
    }
  }

  "WorkersConfigBuilder.modifiers-t4" must "execute each future one at a time and apply throttle, retry and progress correctly" in {
    val allPeriod_ns =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val allAttempts = Array.fill(items.size)(1)

        val result =
          items
            .zipWithIndex
            .workers
            .throttle(DELAY)
            .retry {
              case List(r:RuntimeException) =>
                sched.addEvent(s"retry-${r.getMessage}")
                true.future
              case List(r1:RuntimeException,r2:RuntimeException) =>
                sched.addEvent(s"retry-${r1.getMessage}")
                true.future
              case _ => false.future
            }
            .progress(new ProgressReporter {
              override def onStartTask() = sched.addEvent(s"progress-start")
              override def onCompleteTask() = sched.addEvent(s"progress-end")
              override def onStartStep(stepId: Long) = { }
              override def onCompleteStep(stepId: Long) = sched.addEvent(s"progress-${stepId+1}")
            })
            .map { case (i,idx) =>
              val attempts = allAttempts(idx)
              Future {
                if(attempts < 3) {
                  allAttempts(idx) += 1
                  sched.addEvent(s"map-$i+$attempts")
                  throw new RuntimeException(s"$i+$attempts")
                } else {
                  sched.addEvent(s"map-$i+$attempts")
                  i
                }
              }
            }

        result.get
        // TODO: this doesn't work properly below 1 ms throttle?
  //      waitForActiveExecutionCount(0)

        items foreach { i =>
          sched.happensBefore(s"progress-start",s"map-$i+1") should equal(true)
          sched.happensBefore(s"map-$i+1",s"retry-$i+1") should equal(true)
          sched.happensBefore(s"retry-$i+1",s"map-$i+2") should equal(true)
          sched.happensBefore(s"map-$i+2",s"retry-$i+2") should equal(true)
          sched.happensBefore(s"map-$i+3",s"progress-$i") should equal(true)
          sched.happensBefore(s"progress-$i",s"progress-end") should equal(true)
        }

        val eventMap = sched.eventMap
        (1 until items.size) flatMap { i =>
          val e1 = eventMap(s"map-$i+1")
          val e2 = eventMap(s"map-$i+2")
          val e3 = eventMap(s"map-$i+3")
          val e4 = eventMap(s"map-${i+1}+1")
          Vector(
            e2.elapsed_ns - e1.elapsed_ns,
            e3.elapsed_ns - e2.elapsed_ns,
            e4.elapsed_ns - e3.elapsed_ns
          )
        }
      }

    val filteredPeriod_ns = filterOutliersBy(allPeriod_ns.flatten.map(_.toDouble),{ v:Double => v})
    val avgPeriod_ns = filteredPeriod_ns.sum / filteredPeriod_ns.size
    avgPeriod_ns should equal(DELAY_NS.toDouble +- DELAY_NS * 0.1)
  }

}