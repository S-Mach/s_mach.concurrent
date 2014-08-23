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

import org.scalatest.{Matchers, FlatSpec}

import s_mach.concurrent.util._
import TestBuilder._

import scala.util.{Random, Failure, Success}

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
    concurrentWorkerPercent should be >= 0.90
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

}