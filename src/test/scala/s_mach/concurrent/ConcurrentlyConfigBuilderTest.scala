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

import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._
import scala.util.{Failure, Success, Try}

class ConcurrentlyConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "ConcurrentlyConfigBuilder.map-t4" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.concurrently.map(success)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal (Success(items))
        isConcurrentSchedule(items.size, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= 0.98
  }

  "ConcurrentlyConfigBuilder.flatMap-t5" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.concurrently.flatMap(successN)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
        isConcurrentSchedule(items.size, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= 0.98
  }

  "ConcurrentlyConfigBuilder.foreach-t6" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = items.concurrently.foreach(success)

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
    concurrentPercent should be >= 0.98
  }

}

