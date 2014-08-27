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

class FutureOpsTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "firstSuccess-t1" must "return the first future to successfully complete" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val endLatch = Latch()

      val f3 = Future { throw new RuntimeException }
      val f2 = f3 happensBefore Future { 2 }
      // Note: using endLatch here to prevent race condition between firstSuccess completing and f1 being triggered
      val f1 = endLatch happensBefore Future { 1 }

      val result = Vector(f1,f2,f3).firstSuccess

      waitForActiveExecutionCount(0)
      endLatch.set()

      result.getTry should equal(Success(2))
    }
  }

  "firstSuccess(fail)-t2" must "complete with a failure if all futures fail" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val f1 = Future { throw new RuntimeException("1") }
      val f2 = Future { throw new RuntimeException("2") }
      val f3 = Future { throw new RuntimeException("3") }

      val result = Vector(f1,f2,f3).firstSuccess

      result.getTry shouldBe a [Failure[_]]
    }
  }
}

