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

import s_mach.concurrent.TestBuilder._

import scala.util.{Success, Failure}
import org.scalatest.{FlatSpec, Matchers}
import util._

class TupleConcurrentlyTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "concurrently-t0" must "wait on all Futures to complete concurrently" in {
    val results =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val f1 = success(1)
        val f2 = success(2)
        val f3 = success(3)
        val f4 = success(4)
        val f5 = success(5)
        val f6 = success(6)

        val result = concurrently(f1,f2,f3,f4,f5,f6)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should be(Success((1,2,3,4,5,6)))
        isConcurrentSchedule(Vector(1,2,3,4,5,6), sched)
      }

    val concurrentPercent = results.count(_ == true) / results.size.toDouble
    concurrentPercent should be >= 0.98
  }

  "concurrently-t1" must "complete immediately after any Future fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val f2 = fail(2)
      // Note1: without hooking the end latch here there would be a race condition here between success 1,3,4,5,6
      // and end. The latch is used to create a serialization schedule that can be reliably tested
      // Note2: Due to this design, a bug in merge that does not complete immediately on failure will cause a
      // deadlock here instead of a failing test
      val f1 = endLatch happensBefore success(1)
      val f3 = endLatch happensBefore success(3)
      val f4 = endLatch happensBefore success(4)
      val f5 = endLatch happensBefore success(5)
      val f6 = endLatch happensBefore success(6)

      val result = concurrently(f1,f2,f3,f4,f5,f6)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      endLatch.set()
      waitForActiveExecutionCount(0)

      result.getTry shouldBe a [Failure[_]]
      result.getTry.failed.get shouldBe a [ConcurrentThrowable]

      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      (1 to 6).filter(_ != 2).foreach { i =>
        sched.happensBefore("end", s"success-$i") should equal(true)
      }
    }
  }

  "concurrently-vs-sequence-t2" must "not complete immediately after any Future fails and must throw only the first exception" in {
    test repeat TEST_COUNT run {
     implicit val ctc = mkConcurrentTestContext()
     import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val f2 = fail(2)
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


  "concurrently-t2" must "throw ConcurrentThrowable which can wait for all failures" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val result = concurrently(fail(1),success(2),success(3),fail(4),success(5),fail(6))

      waitForActiveExecutionCount(0)

      val thrown = result.failed.get.asInstanceOf[ConcurrentThrowable]

      // Even though there are two worker threads, it technically is a race condition to see which failure happens
      // first. This actually happens in about 1/1000 runs where it appears worker one while processing fail-1 stalls
      // and worker 2 is able to complete success-2, success-3 and fail-4 before fail-1 finishes
      thrown.firstFailure.toString.startsWith("java.lang.RuntimeException: fail-") should equal(true)
      thrown.allFailure.get.map(_.toString) should contain allOf(
        new RuntimeException("fail-1").toString,
        new RuntimeException("fail-4").toString,
        new RuntimeException("fail-6").toString
      )
    }
  }
}
