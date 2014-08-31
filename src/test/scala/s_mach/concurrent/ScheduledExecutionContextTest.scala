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

class ScheduledExecutionContextTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  Vector(
    (1.second, 10, .0004),
    (100.millis, 100, .004),
    (10.millis, 1000, .04),
    (1.millis, 10000, .05),
    (750.micros, 10000, .01),
    (500.micros, 10000, .01),
    (250.micros, 10000, .01),
    (100.micros, 20000, .01),
    (50.micros, 20000, .01),
    (10.micros, 30000, .06), // TODO: not sure why this particular period fails more often
    (5.micros, 40000, .01),
    (2.micros, 50000, .02)
  ) foreach { case (_delay, testCount, errorPercent) =>
    val delay_ns = _delay.toNanos
//    val scheduledPercent = ScheduledExecutionContext.calcScheduledPercent(delay_ns)
//    val scheduledDelay_ns = ScheduledExecutionContext.calcScheduledDelay_ns(delay_ns)
    s"ScheduledExecutionContext.schedule(${_delay})" must "return a DelayedFuture that executes the task after the supplied delay" in {
//      ScheduledExecutionContext.lateDelayError_ns.set(0)
//      ScheduledExecutionContext.earlyDelayError_ns.set(0)

//      println(s"delay=${_delay}")
//      println("----")
//      println(s"scheduledPercent=$scheduledPercent")
//      println(s"scheduledDelay_ns=$scheduledDelay_ns")

      val allDelay_ns =
        test repeat testCount run {
          implicit val ctc = mkConcurrentTestContext()
          import ctc._
          sched.addEvent("start")
          val result = scheduledExecutionContext.schedule(_delay) { () => sched.addEvent("trigger");1 }
          result.get should equal(1)
          sched.startEvents(0).elapsed_ns - sched.startEvents(1).elapsed_ns
        }

//      println(s"avgLateDelayError_ns=${ScheduledExecutionContext.lateDelayError_ns.get.toDouble/testCount}")
//      println(s"avgEarlyDelayError_ns=${ScheduledExecutionContext.earlyDelayError_ns.get.toDouble/testCount}")
//      println("----")

      val filteredDelay_ns = filterOutliersBy(allDelay_ns.map(_.toDouble), { v:Double => v})
      val avgDelay_ns = filteredDelay_ns.sum / filteredDelay_ns.size
      avgDelay_ns should equal(delay_ns.toDouble +- delay_ns.toDouble * errorPercent)
//      if(scheduledPercent < 1.0)
//        (ScheduledExecutionContext.lateDelayError_ns.get + scheduledDelay_ns) shouldBe <=(delay_ns)
    }
  }
}
