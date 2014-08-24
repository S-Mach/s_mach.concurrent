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

class ConcurrentlyConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "ConcurrentlyConfigBuilder.map-t0" must "execute each future at the same time" in {
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

  "ConcurrentlyConfigBuilder.flatMap-t1" must "execute each future at the same time" in {
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

  "ConcurrentlyConfigBuilder.foreach-t2" must "execute each future at the same time" in {
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

  "ConcurrentlyConfigBuilder.modifiers-t3" must "execute each future one at a time and apply retry and progress correctly" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val allAttempts = Array.fill(items.size)(1)

      val result =
        items
          .zipWithIndex
          .concurrently
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
    }
  }

}

