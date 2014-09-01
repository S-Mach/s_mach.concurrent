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

class SeriallyConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "SeriallyConfigBuilder.map-t1" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.map(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal (Success(items))
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }

  "SeriallyConfigBuilder.flatMap-t2" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.flatMap(successN)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }


  "SeriallyConfigBuilder.foreach-t3" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.foreach(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(()))
      val eventMap = sched.eventMap
      (1 to items.size) foreach { i =>
        eventMap.contains(s"success-$i") should equal(true)
      }
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }

  "SeriallyConfigBuilder.modifiers-t4" must "execute each future one at a time and apply throttle, retry and progress correctly" in {
    val allPeriod_ns =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        var even = true

        val result =
          items
            .serially
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

        sched.orderedEvents.map(_.id) should equal(Vector(
          "progress-0",
          "map-1-true",
          "retry-1",
          "map-1-false",
          "progress-1",
          "map-2-true",
          "retry-2",
          "map-2-false",
          "progress-2",
          "map-3-true",
          "retry-3",
          "map-3-false",
          "progress-3",
          "map-4-true",
          "retry-4",
          "map-4-false",
          "progress-4",
          "map-5-true",
          "retry-5",
          "map-5-false",
          "progress-5",
          "map-6-true",
          "retry-6",
          "map-6-false",
          "progress-6"
        ))

        val eventMap = sched.eventMap
        (1 until items.size) flatMap { i =>
          val e1 = eventMap(s"map-$i-true")
          val e2 = eventMap(s"map-$i-false")
          val e3 = eventMap(s"map-${i+1}-true")
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

