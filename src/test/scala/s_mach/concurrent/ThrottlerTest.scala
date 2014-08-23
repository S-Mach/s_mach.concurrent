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

import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.util._
import scala.concurrent.duration._

import org.scalatest.Matchers
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Try, Success}
import s_mach.concurrent._
import TestBuilder._

class ThrottlerTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  def doTest(testCount: Int, expectedDelay_ns: Long, expectedErr_ns: Long) {
    implicit val ctc = mkConcurrentTestContext()
    import ctc._

    val t = Throttler(expectedDelay_ns)

    val startTime_ns = System.nanoTime()
    val results =
      (1 to testCount)
        .map { i =>
          t.run { () =>
            val now_ns = System.nanoTime()
//              println(i)
            (i, now_ns)
          }
        }
        .merge
        .get


    waitForActiveExecutionCount(0)

    val idToWhen = { results :+ (0, startTime_ns) }.toMap

//      for(i <- 0 to testCount - 1) {
//        val current = idToWhen(i)
//        val next = idToWhen(i+1)
//        println(s"$i -> ${i+1} ${next - current} $expectedDelay_ns ${(next - current) - expectedDelay_ns}")
//      }
    val allDelay_ns =
      for(i <- 1 to testCount - 1) yield {
        val current = idToWhen(i)
        val next = idToWhen(i+1)
        val delay_ns = next - current
        (delay_ns >= expectedDelay_ns) should equal(true)
        delay_ns
      }
    val avgDelay_ns = allDelay_ns.sum.toDouble / testCount
//    println(avgDelay_ns.nanos)
    avgDelay_ns should equal(expectedDelay_ns.toDouble +- expectedErr_ns.toDouble)
  }
  val d = 100.micros.toNanos
  // 100ms => 99.96 0.04% error
  // 10ms => 10.04 0.4% error
  // 1ms => 1.039ms 3% error
  // 200us => 218.7us 9% error
  // 100us => 114us 13% error
  // TODO: this is broken for this specific timing - I believe it has to do with the minimum delay scheduled executor service can reliably handle
  // 50 us => 83.27 40% error
  // 10us => 10.45us 4% error
  // 5us => 5.529us 10% error
  // 3us => 3.463us 14% error
  // 1us => 1.826us 45% error
  val tests = Vector(
    (100.millis, 100, 0.01),
    (10.millis, 1000, 0.01),
    (1.millis, 10000, 0.06),
    (100.micros, 10000, 0.20),
    (10.micros, 10000, 0.15),
    (5.micros, 10000, 0.25)
  )
  tests.foreach { case (delay, testCount, errPercent) =>
    s"throttle-lock-$delay" must s"execute operations no faster than the throttle setting" in {
      val delay_ns = delay.toNanos
      doTest(1000, delay_ns, (delay_ns * errPercent).toLong)
    }
  }
}
