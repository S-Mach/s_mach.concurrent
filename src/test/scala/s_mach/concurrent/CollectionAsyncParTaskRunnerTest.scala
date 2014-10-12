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

import s_mach.concurrent.config.{ThrottleConfig, AsyncConfig}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Random, Failure, Success}
import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.util._
import TestBuilder._

class CollectionAsyncParTaskRunnerTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "CollectionAsyncParTaskRunner-t0" must "build and copy config correctly" in {
    implicit val ctc = mkConcurrentTestContext()

    val items = mkItems

    val progressReporter = new TaskEventListener {
      override def onStartTask(): Unit = ???
      override def onCompleteStep(stepId: Int): Unit = ???
      override def onStartStep(stepId: Int): Unit = ???
      override def onCompleteTask(): Unit = ???
    }

    val retryFn = { _:List[Throwable] => false.future }

    val config1Builder =
      items
        .async
        .par(3)
        .throttle(DELAY)
        .retry(retryFn)
        .progress(progressReporter)


    config1Builder.enumerator should equal(items)
    config1Builder.optTotal should equal(Some(items.size))
    config1Builder.workerCount should equal(3)
    config1Builder.optThrottle should equal(Some(ThrottleConfig(DELAY_NS)(ctc)))
    config1Builder.optRetry should equal(Some(retryFn))
    config1Builder.optProgress should equal(Some(progressReporter))

    val config2Builder =
      items
        .iterator
        .async
        .par(3)

    config2Builder.optTotal should equal(None)

    val config1 = config1Builder.build()
    val config2 = items.async.par.using(config1).build()

    config1 should equal(config2)

    val config3 = AsyncConfig(
      workerCount = config2.workerCount,
      optProgress = config2.optProgress,
      optRetry = config2.optRetry,
      optThrottle = config2.optThrottle
    )

    config3 should equal(config1)
    config3 should equal(config2)
  }

  "CollectionAsyncParTaskRunnerTest.map-t1" must "should invoke futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val items = mkItems
        val result = items.async.par.map(success)

        waitForActiveExecutionCount(0)

        result.getTry should equal (Success(items))
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "CollectionAsyncParTaskRunnerTest.map-t2" must "should invoke at most workerCount futures concurrently" in {
    val cpuCount = Runtime.getRuntime.availableProcessors()
    require(cpuCount >= 3, s"Only $cpuCount CPUs detected. At least 3 are required to run this test.")

//    val result =
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      // Always leave at least one spare cpu to detect if more workers are concurrent than should be
      val workerCount = 2 + Random.nextInt((cpuCount - 2) / 2)
      val lock = new Object
      var maxActiveCount = 0
      var activeCount = 0
      val items = mkItems
      val result = items.async.par(workerCount).map { item =>
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
    }
  }

  "CollectionAsyncParTaskRunnerTest.flatMap-t3" must "execute futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val items = mkItems
        val result = items.async.par.flatMap(successN)

        waitForActiveExecutionCount(0)

        result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "CollectionAsyncParTaskRunnerTest.foreach-t4" must "execute futures concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val items = mkItems
        val result = items.async.par.foreach(success)

        waitForActiveExecutionCount(0)

        result.getTry should equal(Success(()))
        val eventMap = sched.eventMap
        items foreach { i =>
          eventMap.contains(s"success-$i") should equal(true)
        }
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "CollectionAsyncParTaskRunnerTest.map-t5" must "complete immediately after any Future fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val latch = Latch()
      val items = mkItems
      val result = items.zipWithIndex.async.par(2).map { case (item, i) =>
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
      sched.happensBefore("start",s"fail-${items(1)}") should equal(true)
      sched.happensBefore(s"fail-${items(1)}","end") should equal(true)

      // Note: there is a race condition between whether fail causes workers to exit before workers starts up success-3
      (4 to items.size - 1).foreach { i =>
        sched.orderedEvents.exists(_.id == s"success-${items(i)}") should equal (false)
      }
    }
  }

  "CollectionAsyncParTaskRunnerTest.map-t6" must "complete with an exception if the last worker fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val items = mkItems
      val lastIdx = items.size - 1
      val result = items.zipWithIndex.async.par(2).map { case (item, i) =>
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

  "CollectionAsyncParTaskRunner.modifiers-t7" must "execute each future one at a time and apply throttle, retry and progress correctly" in {
    val allPeriod_ns =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val items = mkItems
        val allAttempts = Array.fill(items.size)(1)

        val result =
          items
            .zipWithIndex
            .async
            .par
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
            .progress(new TaskEventListener {
              override def onStartTask() = sched.addEvent(s"progress-start")
              override def onCompleteTask() = sched.addEvent(s"progress-end")
              override def onStartStep(stepId: Int) = { }
              override def onCompleteStep(stepId: Int) = sched.addEvent(s"progress-$stepId")
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

        items.zipWithIndex foreach { case (i,idx) =>
          sched.happensBefore(s"progress-start",s"map-$i+1") should equal(true)
          sched.happensBefore(s"map-$i+1",s"retry-$i+1") should equal(true)
          sched.happensBefore(s"retry-$i+1",s"map-$i+2") should equal(true)
          sched.happensBefore(s"map-$i+2",s"retry-$i+2") should equal(true)
          sched.happensBefore(s"map-$i+3",s"progress-${idx+1}") should equal(true)
          sched.happensBefore(s"progress-${idx+1}",s"progress-end") should equal(true)
        }

        val eventMap = sched.eventMap
        items.take(items.size - 1).zipWithIndex flatMap { case (i,idx) =>
          val e1 = eventMap(s"map-$i+1")
          val e2 = eventMap(s"map-$i+2")
          val e3 = eventMap(s"map-$i+3")
          val e4 = eventMap(s"map-${items(idx+1)}+1")
          Vector(
            e2.elapsed_ns - e1.elapsed_ns,
            e3.elapsed_ns - e2.elapsed_ns,
            e4.elapsed_ns - e3.elapsed_ns
          )
        }
      }

    // TODO: uncomment once precision thottler is available
//    val filteredPeriod_ns = filterOutliersBy(allPeriod_ns.flatten.map(_.toDouble),{ v:Double => v})
//    val avgPeriod_ns = filteredPeriod_ns.sum / filteredPeriod_ns.size
//    avgPeriod_ns should equal(DELAY_NS.toDouble +- DELAY_NS * 0.1)
  }
}