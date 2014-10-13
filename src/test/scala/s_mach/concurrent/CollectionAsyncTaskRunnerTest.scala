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

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._
import s_mach.concurrent.config.AsyncConfig

class CollectionAsyncTaskRunnerTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "CollectionAsyncTaskRunner-t0" must "build and copy config correctly" in {
    implicit val ctc = mkConcurrentTestContext()

    val items = mkItems

    val progressReporter = new TaskEventListener {
      override def onStartTask(): Unit = ???
      override def onCompleteStep(stepId: Int): Unit = ???
      override def onStartStep(stepId: Int): Unit = ???
      override def onCompleteTask(): Unit = ???
    }

    val retryer = new RetryDecider {
      override def shouldRetry(stepId: Long, failure: Throwable): Future[Boolean] = ???
    }

    val config1Builder =
      items
        .async
        .throttle(DELAY)
        .retryDecider(retryer)
        .progress(progressReporter)


    config1Builder.enumerator should equal(items)
    config1Builder.optTotal should equal(Some(items.size))
    config1Builder.optThrottle.nonEmpty should equal(true)
    config1Builder.optThrottle.get.throttle_ns should equal(DELAY.toNanos)
    config1Builder.optThrottle.get.scheduledExecutionContext should equal(ctc)
    config1Builder.optRetry.nonEmpty should equal(true)
    config1Builder.optRetry.get.retryer should equal(retryer)
    config1Builder.optRetry.get.executionContext should equal(ctc)
    config1Builder.optProgress.nonEmpty should equal(true)
    config1Builder.optProgress.get.reporter should equal(progressReporter)
    config1Builder.optProgress.get.executionContext should equal(ctc)

    val config2Builder =
      items
        .iterator
        .async

    config2Builder.optTotal should equal(None)

    val config1 = config1Builder.build()
    val config2 = items.async.using(config1).build()

    config1 should equal(config2)

    val config3 = AsyncConfig(
      optProgress = config2.optProgress,
      optRetry = config2.optRetry,
      optThrottle = config2.optThrottle
    )

    config3 should equal(config1)
    config3 should equal(config2)
  }

  "CollectionAsyncTaskRunner.map-t1" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val items = mkItems
      val result = items.async.map(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal (Success(items))
      isSerialSchedule(items, sched) should equal(true)
    }
  }

  "CollectionAsyncTaskRunner.flatMap-t2" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val items = mkItems
      val result = items.async.flatMap(successN)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
      isSerialSchedule(items, sched) should equal(true)
    }
  }


  "CollectionAsyncTaskRunner.foreach-t3" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val items = mkItems
      val result = items.async.foreach(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(()))
      val eventMap = sched.eventMap
      items foreach { i =>
        eventMap.contains(s"success-$i") should equal(true)
      }
      isSerialSchedule(items, sched) should equal(true)
    }
  }

  "CollectionAsyncTaskRunner.foldLeft-t4" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val items = mkItems
      val result = items.async.foldLeft(0)((acc,item) => success(item).map(_ + acc))

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(items.sum))
      isSerialSchedule(items, sched) should equal(true)
    }
  }

  "CollectionAsyncTaskRunner.modifiers-t5" must "execute each future one at a time and apply throttle, retry and progress correctly" in {
    val allPeriod_ns =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        var even = true

        val items = Vector(1,2,3)//mkItems
        val result =
          items
            .async
            .throttle(DELAY)
            .retry {
              case List(r:RuntimeException) =>
                sched.addEvent(s"retry-${r.getMessage}")
                true.future
              case _ => false.future
            }
            .progress { progress =>
              sched.addEvent(s"progress-${progress.completed}")
            }
            .map { i =>
              sched.addEvent(s"map-$i-$even")
              Future {
                if(even) {
                  even = false
                  throw new RuntimeException(i.toString)
                } else {
                  even = true
                  i
                }
              }
            }

        result.get
        // TODO: this doesn't work properly below 1 ms throttle?
  //      waitForActiveExecutionCount(0)

        sched.orderedEvents.map(_.id) should equal(
          Vector("progress-0") ++
          items.zipWithIndex.flatMap { case (item,idx) =>
            Vector(
              s"map-$item-true",
              s"retry-$item",
              s"map-$item-false",
              s"progress-${idx+1}"
            )
          }
        )

        val eventMap = sched.eventMap
        items.inits.zipWithIndex flatMap { case(item, idx) =>
          val e1 = eventMap(s"map-$item-true")
          val e2 = eventMap(s"map-$item-false")
          val e3 = eventMap(s"map-${items(idx+1)}-true")
          Vector(
            e2.elapsed_ns - e1.elapsed_ns,
            e3.elapsed_ns - e2.elapsed_ns
          )
        }
      }

    // TODO: uncomment once precision thottler is available
//    val filteredPeriod_ns = filterOutliersBy(allPeriod_ns.flatten.map(_.toDouble),{ v:Double => v})
//    val avgPeriod_ns = filteredPeriod_ns.sum / filteredPeriod_ns.size
//    avgPeriod_ns should equal(DELAY_NS.toDouble +- DELAY_NS * 0.1)
  }

  "CollectionAsyncTaskRunner.modifiers-foldLeft-t6" must "execute each future one at a time and apply throttle, retry and progress correctly" in {
    val allPeriod_ns =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        var even = true

        val items = Vector(1,2,3)//mkItems
        val result =
          items
            .async
            .throttle(DELAY)
            .retry {
              case List(r:RuntimeException) =>
                sched.addEvent(s"retry-${r.getMessage}")
                true.future
              case _ => false.future
            }
            .progress { progress =>
              sched.addEvent(s"progress-${progress.completed}")
            }
            .foldLeft(0) { (acc,i) =>
              sched.addEvent(s"map-$i-$even")
              Future {
                if(even) {
                  even = false
                  throw new RuntimeException(i.toString)
                } else {
                  even = true
                  acc + i
                }
              }
            }

        result.get
        // TODO: this doesn't work properly below 1 ms throttle?
  //      waitForActiveExecutionCount(0)

        sched.orderedEvents.map(_.id) should equal(
          Vector("progress-0") ++
          items.zipWithIndex.flatMap { case (item,idx) =>
            Vector(
              s"map-$item-true",
              s"retry-$item",
              s"map-$item-false",
              s"progress-${idx+1}"
            )
          }
        )

        val eventMap = sched.eventMap
        items.inits.zipWithIndex flatMap { case(item, idx) =>
          val e1 = eventMap(s"map-$item-true")
          val e2 = eventMap(s"map-$item-false")
          val e3 = eventMap(s"map-${items(idx+1)}-true")
          Vector(
            e2.elapsed_ns - e1.elapsed_ns,
            e3.elapsed_ns - e2.elapsed_ns
          )
        }
      }

    // TODO: uncomment once precision thottler is available
//    val filteredPeriod_ns = filterOutliersBy(allPeriod_ns.flatten.map(_.toDouble),{ v:Double => v})
//    val avgPeriod_ns = filteredPeriod_ns.sum / filteredPeriod_ns.size
//    avgPeriod_ns should equal(DELAY_NS.toDouble +- DELAY_NS * 0.1)
  }

}

