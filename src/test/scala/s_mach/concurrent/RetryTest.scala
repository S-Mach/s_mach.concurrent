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
import scala.concurrent.duration._
import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._

import scala.util.Failure

class RetryTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "RetryConfig" must "retry failed iterations of the computation" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      var attempts = 0

      val items = mkItems
      val result =
        items
          .async
          .retry {
            case List(r1:RuntimeException) =>
              sched.addEvent(s"$r1")
              true.future
            case List(r1:RuntimeException, r2:RuntimeException) =>
              sched.addEvent(s"$r1 $r2")
              true.future
            // Fail after 3 retries
            case _ =>
              sched.addEvent("fail")
              false.future
          }
          .foreach { _ =>
            attempts = attempts + 1
            sched.addEvent(s"attempt-$attempts")
            Future {
              throw new RuntimeException("failed")
            }
          }

      waitForActiveExecutionCount(0)

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [RuntimeException]

      sched.orderedEvents.map(_.id) should equal(Vector(
        "attempt-1",
        "java.lang.RuntimeException: failed",
        "attempt-2",
        "java.lang.RuntimeException: failed java.lang.RuntimeException: failed",
        "attempt-3",
        "fail"
      ))
    }
  }

  "RetryConfig" must "end immediately for failures that occur outside the computation" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      var attempts = 0

      val items = mkItems
      val result =
        items
          .async
          .retry {
            case List(r1:RuntimeException) =>
              sched.addEvent(s"$r1")
              true.future
            case List(r1:RuntimeException, r2:RuntimeException) =>
              sched.addEvent(s"$r1 $r2")
              true.future
            // Fail after 3 retries
            case _ =>
              sched.addEvent("fail")
              false.future
          }
          .foreach { _ =>
            attempts = attempts + 1
            sched.addEvent(s"attempt-$attempts")
            throw new RuntimeException("failed")
          }

      waitForActiveExecutionCount(0)

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [RuntimeException]

      sched.orderedEvents.map(_.id) should equal(Vector(
        "attempt-1"
      ))
    }
  }
}
