/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____       __  ___              __
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

import s_mach.concurrent.TestBuilder._

import scala.util.Failure
import org.scalatest.{FlatSpec, Matchers}
import util._

import scala.concurrent.Future

class FutureSequenceProofTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "Future.sequence-t2" must "not complete immediately after any Future fails and must throw only the first exception" in {
    test repeat TEST_COUNT run {
     implicit val ctc = mkConcurrentTestContext()
     import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val f2 : Future[Int] = fail(2)
      // Note1: because the for-comprehension will not proceed from waiting on f1 until it completes, a deadlock
      // will result if the end latch is used here like in the concurrently test above.
      // Note2: because the for-comprehension won't detect the failure until after f1 completes, the success-1 and
      // fail-2 events will always happen before end. This isn't true for concurrently above
      val f1 = f2 happensBefore success(1)
      val f3 = endLatch happensBefore success(3)
      val f4 = endLatch happensBefore success(4)
      val f5 = endLatch happensBefore success(5)
      val f6 = endLatch happensBefore success(6)

      val result =
        for {
          i1 <- f1
          i2 <- f2
          i3 <- f3
          i4 <- f4
          i5 <- f5
          i6 <- f6
        } yield (i1,i2,i3,i4,i5,i6)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      endLatch.set()
      waitForActiveExecutionCount(0)

      result.getTry shouldBe a[Failure[_]]
      result.getTry.failed.get.toString should equal(new RuntimeException("fail-2").toString)

      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      sched.happensBefore("start","success-1") should equal(true)
      sched.happensBefore("success-1","end") should equal(true)

      (3 to 6).foreach { i =>
        sched.happensBefore("end", s"success-$i") should equal(true)
      }
    }
  }
}
