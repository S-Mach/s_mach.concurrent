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
         .t1i .,::;;; ;1tt        Copyright (c) 2017 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent

import scala.concurrent.Future
import scala.util.{Success, Failure}
import org.scalatest.{FlatSpec, Matchers}
import util._

import s_mach.concurrent.impl.MergeOps
import TestBuilder._

class FlatMergeTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "flatMerge-t0" must "wait on all Futures to complete concurrently" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val result = MergeOps.flatMerge(Vector(successN(1),successN(2),successN(3),successN(4),successN(5),successN(6)))

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.awaitTry should be(Success(Vector(1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6)))

        isConcurrentSchedule(Vector(1,2,3,4,5,6), sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "flatMerge-t1" must "complete immediately after any Future fails" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val f2 = fail(2)
      // Note1: without hooking the end latch here there would be a race condition here between successN 1,3,4,5,6
      // and end. The latch is used to create a serialization schedule that can be reliably tested
      // Note2: Due to this design, a bug in flatMerge that does not complete immediately on failure will cause a
      // deadlock here instead of a failing test
      val f1 = endLatch happensBefore successN(1)
      val f3 = endLatch happensBefore successN(3)
      val f4 = endLatch happensBefore successN(4)
      val f5 = endLatch happensBefore successN(5)
      val f6 = endLatch happensBefore successN(6)

      val result = MergeOps.flatMerge(Vector(f1,f2,f3,f4,f5,f6))

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      endLatch.set()
      waitForActiveExecutionCount(0)

      result.awaitTry shouldBe a [Failure[_]]
      result.awaitTry.failed.get shouldBe a [AsyncParThrowable]

      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      (1 to 6).filter(_ != 2).foreach { i =>
        sched.happensBefore("end", s"success-$i") should equal(true)
      }
    }
  }

  "flatMerge-vs-sequence-t2" must "not complete immediately after any Future fails and must throw only the first exception" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")
      val endLatch = Latch()

      val f2 = fail(2)
      // Note1: because Future.sequence will not proceed from waiting on f1 until it completes, a deadlock will
      // result if the end latch is used here like in the flatMerge test above.
      // Note2: because Future.sequence won't detect the failure until after f1 completes, the successN-1 and fail-2
      // events will always happen before end. This isn't true for flatMerge above
      val f1 = f2 happensBefore successN(1)
      val f3 = endLatch happensBefore successN(3)
      val f4 = endLatch happensBefore successN(4)
      val f5 = endLatch happensBefore successN(5)
      val f6 = endLatch happensBefore successN(6)

      val result = Future.sequence(Vector(f1,f2,f3,f4,f5,f6)).map(_.flatten)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")
      endLatch.set()
      waitForActiveExecutionCount(0)

      result.awaitTry shouldBe a [Failure[_]]
      result.awaitTry.failed.get.toString should equal(new RuntimeException("fail-2").toString)

      sched.happensBefore("start","fail-2") should equal(true)
      sched.happensBefore("fail-2","end") should equal(true)

      sched.happensBefore("start","success-1") should equal(true)
      sched.happensBefore("success-1","end") should equal(true)

      (3 to 6).foreach { i =>
        sched.happensBefore("end", s"success-$i") should equal(true)
      }
    }
  }


  "flatMerge-t3" must "throw AsyncParThrowable which can wait for all failures" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val result = MergeOps.flatMerge(Vector(fail(1),successN(2),successN(3),fail(4),successN(5),fail(6)))

      val thrown = result.awaitTry.failed.get.asInstanceOf[AsyncParThrowable]

      thrown.firstFailure.toString.startsWith("java.lang.RuntimeException: fail-") should equal(true)
      thrown.allFailure.await.map(_.toString) should contain allOf(
        new RuntimeException("fail-1").toString,
        new RuntimeException("fail-4").toString,
        new RuntimeException("fail-6").toString
      )
    }
  }
}