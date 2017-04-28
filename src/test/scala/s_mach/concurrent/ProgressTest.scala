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

import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._

class ProgressTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "SimpleProgressReporter" must "report progress for each completed report" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val items = mkItems
      val latches = (0 to items.size).map(_ => Latch())

      items
        .zipWithIndex
        .async.par
        .progress { progress =>
          val eventId = s"report-${progress.completed}-${progress.optTotal.getOrElse(0)}"
          sched.addEvent(eventId)
          latches(progress.completed.toInt).set()
        }
        .foreach { case (_,idx) =>
          latches(idx).future
        }

      waitForActiveExecutionCount(0)

      val size = items.size
      (0 until size) foreach { i =>
        sched.happensBefore(s"report-$i-$size",s"report-${i+1}-$size") should equal(true)
      }
    }
  }

  "PeriodicProgressReporter" must "report progress for computations with a known size" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val items = mkItems
      val latches = (0 to items.size).map(_ => Latch())

      items
        .zipWithIndex
        .async.par
        .progress(DELAY) { progress =>
          val eventId = s"report-${progress.completed}-${progress.optTotal.getOrElse(0)}"
          // Note: same progress may be reported twice
          if(sched.startEvents.exists(_.id == eventId) == false) {
            sched.addEvent(eventId)
            latches(progress.completed.toInt).set()
          }
        }
        .foreach { case (_,idx) =>
          latches(idx).future
        }

      waitForActiveExecutionCount(0)

      val size = items.size
      (1 until size) foreach { i =>
        sched.happensBefore(s"report-$i-$size",s"report-${i+1}-$size") should be (true)
      }

    }
  }

  "PeriodicProgressReporter" must "report progress for computations with a unknown size" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val items = mkItems
      val latches = (0 to items.size).map(_ => Latch())

      items.iterator
        .zipWithIndex
        .async
        .progress(DELAY) { progress =>
          val eventId = s"report-${progress.completed}-${progress.optTotal.getOrElse(0)}"
          // Note: same progress may be reported twice
          sched.addEvent(eventId, ignoreIfExist = true)
          latches(progress.completed).trySet()
          ()
        }
        .foreach { case (_,idx) =>
          latches(idx).future
        }

      waitForActiveExecutionCount(0)

      val size = items.size
      (1 until size) foreach { i =>
       sched.happensBefore(s"report-$i-0",s"report-${i+1}-0") should be (true)
      }
    }
  }

//  "PeriodicProgressReporter" must "on average report progress at the specified interval " taggedAs(DelayAccuracyTest) in {
//    val allReportIntervals =
//      test repeat TEST_COUNT run {
//        implicit val ctc = mkConcurrentTestContext()
//        import ctc._
//
//        val items = mkItems
//        val latches = (0 to items.size).map(_ => Latch())
//
//       items
//          .zipWithIndex
//          .async.par
//          .progress(DELAY) { progress =>
//            val eventId = s"report-${progress.completed}-${progress.optTotal.getOrElse(0)}"
//            // Note: same progress may be reported twice
//            if(sched.startEvents.exists(_.id == eventId) == false) {
//              sched.addEvent(eventId)
//              latches(progress.completed.toInt).set()
//            }
//          }
//          .foreach { case (_,idx) =>
//            latches(idx).future
//          }
//
//        waitForActiveExecutionCount(0)
//
//        val size = items.size
//        val eventMap = sched.eventMap
//        (0 until size) map { i =>
//          val e1 = eventMap(s"report-$i-$size")
//          val e2 = eventMap(s"report-${i+1}-$size")
//          e2.elapsed_ns - e1.elapsed_ns
//        }
//      }
//
//    val reportIntervals = allReportIntervals.flatten.map(_.toDouble)
//    val filteredReportIntervals = filterOutliersBy(reportIntervals, { v:Double => v })
//    val avgReportInterval_ns = filteredReportIntervals.sum / filteredReportIntervals.size
//    avgReportInterval_ns should equal(DELAY_NS.toDouble +- DELAY_NS * 0.25)
//  }
}