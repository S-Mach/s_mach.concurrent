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
package s_mach.concurrent.util

import java.util.concurrent.ExecutorService
import scala.concurrent.ExecutionContext
import s_mach.concurrent.impl.FutureOps
import s_mach.concurrent.{PeriodicTask, DelayedFuture, ScheduledExecutionContext}

import scala.concurrent.duration.Duration

/**
 * A context for testing concurrent code that provides:
 * 1) an ExecutionContext that keeps track of the number of active and completed Runnables
 * 2) a Timer for tracking elapsed duration since start of test
 * 3) a SerializationSchedule for detecting order of execution of events
 * 4) a precision delay function that accumulates any delay error
 */
trait ConcurrentTestContext extends ExecutionContext with ScheduledExecutionContext {
  /** @return the current number of active Runnables and scheduled tasks being processed */
  def activeExecutionCount: Int

  /** @return a Timer that is started on construction of the test context */
  implicit def timer: Timer
  /** @return the SerializationSchedule for the context */
  implicit def sched: SerializationSchedule[String]

  /** precisely delay for a period of time in nanoseconds. If the elapsed time differs from the requested the delay,
    * the error in delay is accumulated in delayError_ns */
  def delay(delay_ns: Long) : Unit
  /** @return the accumulated delay error */
  def delayError_ns : Long

  /** Sleep until the active execution count is equal to or less than the specified value */
  def waitForActiveExecutionCount(_activeRunnableCount: Int) {
    while(this.activeExecutionCount > _activeRunnableCount) {
      Thread.sleep(1)
    }
  }
}

object ConcurrentTestContext {
  def apply(ex: ExecutorService) : ConcurrentTestContext = {
    implicit val ec = ExecutionContext.fromExecutor(ex)
    implicit val sec = ScheduledExecutionContext(3)
    apply()
  }
  def apply()(implicit
    ec: ExecutionContext,
    sec: ScheduledExecutionContext
  ) : ConcurrentTestContext = new ConcurrentTestContext {
    val _activeExecutionCount = new java.util.concurrent.atomic.AtomicInteger(0)
    override def activeExecutionCount = _activeExecutionCount.get

//    val _completedRunnableCount = new java.util.concurrent.atomic.AtomicInteger(0)
//    override def completedRunnableCount = _completedRunnableCount.get

//    val _maxRunnableCount = new java.util.concurrent.atomic.AtomicInteger(0)
//    override def maxRunnableCount = _maxRunnableCount.get

    val scheduledExecutionContext = {
        ScheduledExecutionContextListener(sec)
          .onStart.add(1) { (_,_) =>
            _activeExecutionCount.incrementAndGet()
          }
          .onComplete.add(1) { (_,_) =>
            _activeExecutionCount.decrementAndGet()
          }
    }

    // Use a wrapper here to gather all futures to ensure we wait for their completion before shutting down
    // executor
    val executionContext = {
      ExecutionContextListener(ec)
        .onExec.add(1) { (_,_) =>
        _activeExecutionCount.incrementAndGet()
//          val newActiveRunnableCount = _activeRunnableCount.incrementAndGet()
//          _maxRunnableCount.synchronized {
//            if(_maxRunnableCount.get < newActiveRunnableCount) {
//              _maxRunnableCount.getAndSet(newActiveRunnableCount)
//            }
//          }
        }
        .onComplete.add(1) { (_,_) =>
          _activeExecutionCount.decrementAndGet()
//          _completedRunnableCount.incrementAndGet()
        }
    }



    override def scheduleAtFixedRate[U](initialDelay: Duration, period: Duration)(task: () => U) =
      scheduledExecutionContext.scheduleAtFixedRate(initialDelay, period)(task)
    override def schedule[A](delay: Duration)(f: () => A) =
      scheduledExecutionContext.schedule(delay)(f)

    override def reportFailure(cause: Throwable) = executionContext.reportFailure(cause)
    override def execute(runnable: Runnable) = executionContext.execute(runnable)

    override implicit val timer = Timer()
    // Run first tests with no delay to compute avg base line
    override implicit val sched = SerializationSchedule[String]()

    val _delayError_ns = new java.util.concurrent.atomic.AtomicLong(0)

    override def delayError_ns = _delayError_ns.get

    override def delay(delay_ns: Long) = {
      val err_ns = FutureOps.nanoSpinDelay(delay_ns)
      _delayError_ns.addAndGet(err_ns)
    }
  }
}

