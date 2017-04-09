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

import org.scalatest.FlatSpec
import s_mach.concurrent.util._
import org.scalatest.Matchers

class ThrottlerTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  s"throttle" must s"execute operations no faster than the throttle setting" in {
    implicit val ctc = mkConcurrentTestContext()
    import ctc._

    val t = Throttler(DELAY_NS)

    (1 to TEST_COUNT)
      .map { i =>
        t.run {
          sched.addEvent(s"trigger-$i")
          ().future
        }
      }
      .merge
      .await


    waitForActiveExecutionCount(0)

    val eventMap = sched.eventMap
    (1 to TEST_COUNT - 1) foreach { i =>
      val e1 = eventMap(s"trigger-$i")
      val e2 = eventMap(s"trigger-${i+1}")
      val actualDelay_ns = e2.elapsed_ns - e1.elapsed_ns
      actualDelay_ns should be >= DELAY_NS
      actualDelay_ns
    }
  }

//  Vector(
//    (1.second, 10, .21),
//    (100.millis, 100, .18),
//    (10.millis, 1000, .07),
//    (1.millis, 10000, .26),
//    (750.micros, 10000, .25),
//    (500.micros, 10000, .29),
//    (250.micros, 10000, .47),
//    (100.micros, 20000, .97),
//    (50.micros, 20000, 2.78),
//    (10.micros, 30000, 5.89),
//    (5.micros, 40000, 5.70),
//    (2.micros, 50000, 10.92)
//  ).foreach { case (delay, testCount, errorPercent) =>
//    s"throttle-$delay" must s"execute operations on average at the specified rate" taggedAs(DelayAccuracyTest) in {
//      val delay_ns = delay.toNanos
//      implicit val ctc = mkConcurrentTestContext()
//      import ctc._
//
//      val t = Throttler(delay_ns)
//
//      (1 to testCount)
//        .map { i =>
//          t.run {
//            sched.addEvent(s"trigger-$i")
//            ().future
//          }
//        }
//        .merge
//        .get
//
//
//      waitForActiveExecutionCount(0)
//
//      val eventMap = sched.eventMap
//      val allDelay_ns =
//        for(i <- 1 to testCount - 1) yield {
//          val e1 = eventMap(s"trigger-$i")
//          val e2 = eventMap(s"trigger-${i+1}")
//          val actualDelay_ns = e2.elapsed_ns - e1.elapsed_ns
//          actualDelay_ns should be >= delay_ns
//          actualDelay_ns
//        }
//
//      val filteredDelay_ns = filterOutliersBy(allDelay_ns.map(_.toDouble), { d:Double => d })
//      val avgDelay_ns = filteredDelay_ns.sum / testCount
//      avgDelay_ns should equal(delay_ns.toDouble +- (delay_ns * errorPercent))
//    }
//  }
}
