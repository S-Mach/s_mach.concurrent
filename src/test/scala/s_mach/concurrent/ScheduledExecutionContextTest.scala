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
    (1.second, 10),
    (100.millis, 100),
    (10.millis, 1000),
    (1.millis, 10000),
     (500.micros, 10000),
    (100.micros, 10000),
    (50.micros, 10000),
    (10.micros, 20000),
    (5.micros, 20000),
    (2.micros, 30000)
  ) foreach { case (_delay, testCount) =>
    val delay_ns = _delay.toNanos

    s"ScheduledExecutionContext.schedule(${_delay})" must "return a DelayedFuture that executes the task after the supplied delay" in {
//      ScheduledExecutionContext.minScheduledDelay_ns.set(0)
//      ScheduledExecutionContext.minScheduledDelayDecay_ns.set(0)

//      println(s"TESTING ${_delay}")
      val allDelay_ns =
        test repeat testCount run {
          implicit val ctc = mkConcurrentTestContext()
          import ctc._
          val result = scheduledExecutionContext.schedule(_delay) { () => sched.addEvent("trigger");1 }
          result.get should equal(1)
          sched.startEvents(0).elapsed_ns
        }

      val filteredDelay_ns = filterOutliersBy(allDelay_ns.map(_.toDouble), { v:Double => v})
      val avgDelay_ns = filteredDelay_ns.sum / filteredDelay_ns.size
      avgDelay_ns should equal(delay_ns.toDouble +- delay_ns.toDouble * 0.1)
//      println(s"MIN_SCHEDULED_DELAY=${ScheduledExecutionContext.minScheduledDelay_ns.get}")
    }

  }
}
