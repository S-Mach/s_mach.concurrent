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

import scala.concurrent.duration._
import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._
import PeriodicTask._

class ScheduledExecutionContextTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  s"ScheduledExecutionContext.schedule" must "return a DelayedFuture that executes the task after at least the specified delay" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._
      sched.addEvent("start")
      val result = scheduledExecutionContext.schedule(DELAY) { sched.addEvent("trigger");1 }

      ctc.waitForActiveExecutionCount(0)

      result.await should equal(1)
      Math.abs(sched.startEvents(1).elapsed_ns - sched.startEvents(0).elapsed_ns) should be >= DELAY_NS
    }
  }

  s"ScheduledExecutionContext.scheduleCancellable" must "return a DelayedFuture that executes the task after at least the specified delay" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._
      sched.addEvent("start")
      val result = scheduledExecutionContext.scheduleCancellable(DELAY,2) { sched.addEvent("trigger");1 }

      ctc.waitForActiveExecutionCount(0)

      result.await should equal(1)
      result.isCancelled should equal(false)
      result.canCancel should equal(false)
      result.cancel() should equal(false)
      Math.abs(sched.startEvents(1).elapsed_ns - sched.startEvents(0).elapsed_ns) should be >= DELAY_NS
    }
  }

  s"ScheduledExecutionContext.schedule" must "return a DelayedFuture that when cancelled returns the fallback value" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._
      sched.addEvent("start")
      val result = scheduledExecutionContext.scheduleCancellable(10.seconds,2) { sched.addEvent("trigger");1 }
      result.canCancel should equal(true)
      result.cancel() should equal(true)

      ctc.waitForActiveExecutionCount(0)

      result.isCancelled should equal(true)
      result.canCancel should equal(false)
      result.cancel() should equal(false)
      result.await should equal(2)
    }
  }

//  Vector(
//    (1.second, 10, .0003),
//    (100.millis, 100, .003),
//    (10.millis, 1000, .03),
//    (1.millis, 10000, .20),
//    (750.micros, 10000, .20),
//    (500.micros, 10000, .21),
//    (250.micros, 10000, .35),
//    (100.micros, 20000, .70),
//    (50.micros, 20000, 2.15),
//    (10.micros, 30000, 6.62),
//    (5.micros, 40000, 5.9),
//    (2.micros, 50000, 2.89)
//  ) foreach { case (_delay, testCount, errorPercent) =>
//    val delay_ns = _delay.toNanos
//    s"ScheduledExecutionContext.schedule(${_delay})" must "on average, return a DelayedFuture that executes the task after the supplied delay within the expected error percent" taggedAs(DelayAccuracyTest) in {
//      val allDelay_ns =
//        test repeat testCount run {
//          implicit val ctc = mkConcurrentTestContext()
//          import ctc._
//          sched.addEvent("start")
//          val result = scheduledExecutionContext.schedule(_delay) { sched.addEvent("trigger");1 }
//          result.get should equal(1)
//          sched.startEvents(0).elapsed_ns - sched.startEvents(1).elapsed_ns
//        }
//
//      val filteredDelay_ns = filterOutliersBy(allDelay_ns.map(_.toDouble), { v:Double => v})
//      val avgDelay_ns = filteredDelay_ns.sum / filteredDelay_ns.size
//      avgDelay_ns should equal(delay_ns.toDouble +- delay_ns.toDouble * errorPercent)
//    }
//  }

  s"ScheduledExecutionContext.scheduleAtFixedRate" must "return a PeriodicTask that continuously executes the task with at least the specified period separating executions of the task and can be cancelled" in {
    implicit val ctc = mkConcurrentTestContext()
    import ctc._
    val counter = new java.util.concurrent.atomic.AtomicLong(0)
    val latch = Latch()
    // TODO: test specifically for initial delay
    val periodicTask = scheduledExecutionContext.scheduleAtFixedRate(0.nanos,DELAY) { () =>
      // Note: if task happens fast enough there may be several extra iterations after the latch is set
      counter.incrementAndGet() match {
        case i if i <= TEST_COUNT =>
          sched.addEvent(s"trigger-$i")
        case _ => latch.trySet()
      }
    }

    periodicTask.state shouldBe a [Running]

    latch.future.await

    // Running => Cancelled
    periodicTask.cancel() should equal(true)

    waitForActiveExecutionCount(0)

    periodicTask.state should equal(Cancelled)

    sched.startEvents.size should equal(TEST_COUNT)

    // TODO: this test fails miserably
//    val events = sched.orderedEvents
//    (0 until TEST_COUNT - 1) map { i =>
//      val e1 = events(i)
//      val e2 = events(i+1)
//      val actualPeriod_ns = e2.elapsed_ns - e1.elapsed_ns
//
////      actualPeriod_ns should be >= DELAY_NS
//    }
  }

  s"ScheduledExecutionContext.scheduleAtFixedRate" must "return a paused PeriodicTask when requested and resume" in {
    implicit val ctc = mkConcurrentTestContext()
    import ctc._
    val counter = new java.util.concurrent.atomic.AtomicLong(0)
    val latch = Latch()
    val periodicTask = scheduledExecutionContext.scheduleAtFixedRate(0.nanos,DELAY, paused = true) { () =>
      // Note: if task happens fast enough there may be several extra iterations after the latch is set
      counter.incrementAndGet() match {
        case i if i <= TEST_COUNT =>
          sched.addEvent(s"trigger-$i")
        case _ => latch.trySet()
      }
    }

    val state = periodicTask.state
    state shouldBe a [Paused]

    // Paused => Running
    state.asInstanceOf[Paused].resume() should equal(true)

    periodicTask.state shouldBe a [Running]

    latch.future.await

    periodicTask.cancel()

    waitForActiveExecutionCount(0)
  }

  s"ScheduledExecutionContext.scheduleAtFixedRate" must "return a PeriodicTask that can pause when requested" in {
    implicit val ctc = mkConcurrentTestContext()
    import ctc._
    val counter = new java.util.concurrent.atomic.AtomicLong(0)
    val latch1 = Latch()
    val latch2 = Latch()
    val latch3 = Latch()
    val latch4 = Latch()
    val periodicTask = scheduledExecutionContext.scheduleAtFixedRate(0.nanos,DELAY) { () =>
      counter.incrementAndGet() match {
        case 1 =>
          latch1.set()
          latch2.spinUntilSet()
        case 2 =>
          latch3.set()
          latch4.spinUntilSet()
        case _ =>
      }

    }

    periodicTask.state shouldBe a [Running]

    latch1.spinUntilSet()

    counter.get should equal(1)

    // Running => Paused
    periodicTask.state.asInstanceOf[Running].pause() should equal(true)
    latch2.set()

    periodicTask.state shouldBe a [Paused]
    counter.get should equal(1)

    // Paused => Running
    periodicTask.state.asInstanceOf[Paused].resume() should equal(true)

    latch3.spinUntilSet()

    periodicTask.state shouldBe a [Running]
    counter.get should equal(2)

    latch4.set()

    // Paused => Cancelled
    periodicTask.state.asInstanceOf[Running].pause() should equal(true)
    periodicTask.cancel() should equal(true)

    periodicTask.state should equal(PeriodicTask.Cancelled)
    waitForActiveExecutionCount(0)
  }
//
//  Vector(
//    (1.second, 10, .0002),
//    (100.millis, 100, .001),
//    (10.millis, 1000, .01),
//    (1.millis, 10000, .01),
//    (750.micros, 10000, .01),
//    (500.micros, 10000, .01),
//    (250.micros, 10000, .01),
//    (100.micros, 20000, .01),
//    (50.micros, 20000, .02),
//    (10.micros, 30000, .69),
//    (5.micros, 40000, .60),
//    (2.micros, 50000, .75)
//  ) foreach { case (_delay, testCount, errorPercent) =>
//    val delay_ns = _delay.toNanos
//    s"ScheduledExecutionContext.scheduleAtFixedRate(${_delay})" must "return a PeriodicTask that continuously executes the task at the specified period" taggedAs(DelayAccuracyTest) in {
//      implicit val ctc = mkConcurrentTestContext()
//      import ctc._
//      val counter = new java.util.concurrent.atomic.AtomicLong(0)
//      val latch = Latch()
//      // TODO: test specifically for initial delay
//      val periodicTask = scheduledExecutionContext.scheduleAtFixedRate(_delay,_delay) { () =>
//        // Note: if task happens fast enough there may be several extra iterations after the latch is set
//        counter.incrementAndGet() match {
//          case i if i <= testCount =>
//            sched.addEvent(s"trigger-$i")
//          case _ => latch.trySet()
//        }
//      }
//
//      latch.future.get
//
//      periodicTask.cancel()
//
//      waitForActiveExecutionCount(0)
//
//      sched.startEvents.size should equal(testCount)
//
//      val events = sched.orderedEvents
//      val allDelay_ns =
//        (0 until testCount - 1) map { i =>
//          val e1 = events(i)
//          val e2 = events(i+1)
//          e2.elapsed_ns - e1.elapsed_ns
//        }
//
//      val filteredDelay_ns = filterOutliersBy(allDelay_ns.map(_.toDouble), { v:Double => v})
//      val avgDelay_ns = filteredDelay_ns.sum / filteredDelay_ns.size
//      avgDelay_ns should equal(delay_ns.toDouble +- delay_ns.toDouble * errorPercent)
//    }
//  }
}
