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
package s_mach.concurrent.impl

import java.util.concurrent.{ScheduledFuture, TimeUnit, ScheduledExecutorService}
import scala.concurrent.{Promise, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try
import s_mach.concurrent.{PeriodicTask, ScheduledExecutionContext, DelayedFuture}
import s_mach.concurrent.util.{Latch, DelegatedFuture}

object ScheduledExecutionContextImpl {
  // TODO: tune these constants at runtime - they were tuned using a specific hardware environment
  // Note: these parameters ultimately trade cpu time for better precision in delays. In long delays (10ms+), trading
  // cpu time for precision rarely matters, but for shorter delays (micros) this can be the only way to get any kind of
  // reasonable precision. The scheduled percent formula takes this into account, giving a much higher scheduled delay
  // percent for longer delays and lower delays for
  val MIN_SPIN_DELAY_NS = 200
  // TODO: this can be computed at startup by running a scheduled delay for 1 ns and computing mean late error
  val ALWAYS_SPIN_DELAY_NS = 12.micros.toNanos//10.micros.toNanos
  val MAX_SCHEDULED_NS = 2.millis.toNanos
  val MIN_SCHEDULED_NS = 12.micros.toNanos//16.micros.toNanos//8.micros.toNanos
  val SCHEDULED_DELAY_LOG_CONSTANT = Math.log10(MIN_SCHEDULED_NS)
  val SCHEDULED_DELAY_LOG_COEFF = 1.0 / (Math.log10(MAX_SCHEDULED_NS) - SCHEDULED_DELAY_LOG_CONSTANT)
  def calcScheduledPercent(delay_ns: Long) : Double = {
    Math.max(
      0.0,
      Math.min(
        1.0,
        (Math.log10(delay_ns.toDouble) - SCHEDULED_DELAY_LOG_CONSTANT) * SCHEDULED_DELAY_LOG_COEFF
      )
    )
  }
  def calcScheduledDelay_ns(delay_ns: Long) : Long = {
    Math.max(0, (delay_ns * calcScheduledPercent(delay_ns)).toLong - ALWAYS_SPIN_DELAY_NS)
  }

//  val lateDelayError_ns = new java.util.concurrent.atomic.AtomicLong(0)
//  val earlyDelayError_ns = new java.util.concurrent.atomic.AtomicLong(0)

  def nanoDelayUntil(nextEvent_ns: Long): Unit = {
    val done_ns = nextEvent_ns - MIN_SPIN_DELAY_NS
    while(System.nanoTime() < done_ns) { }
  }

  case class ScheduledDelayedFutureImpl[A](
    f: () => A,
    delay: Duration,
    scheduledDelay_ns: Long,
    scheduledExecutorService: ScheduledExecutorService
  )(implicit ec:ExecutionContext) extends DelayedFuture[A] with DelegatedFuture[A] {
    val delay_ns = delay.toNanos
    val expectedTime_ns = System.nanoTime + scheduledDelay_ns
    val startTime_ns = System.nanoTime + delay_ns
    val promise = Promise[A]()

    val javaScheduledFuture =
      scheduledExecutorService.schedule(
        new Runnable {
          override def run() = {
  //            val now = System.nanoTime
  //            if(now < expectedTime_ns) {
  //              earlyDelayError_ns.addAndGet(expectedTime_ns - now)
  //            } else if(now > expectedTime_ns) {
  //              lateDelayError_ns.addAndGet(now - expectedTime_ns)
  //            }
            nanoDelayUntil(startTime_ns)
            promise.complete(Try(f()))
          }
        },
        scheduledDelay_ns,
        TimeUnit.NANOSECONDS
      )

    val delegate = promise.future

  }

  case class SpinDelayedFutureImpl[A](
    f: () => A,
    delay: Duration
  )(implicit ec:ExecutionContext) extends DelayedFuture[A] with DelegatedFuture[A] {
    val promise = Promise[A]()
    val delegate = promise.future

    val startTime_ns = System.nanoTime() + delay.toNanos

    // Spin in this thread - tried this in a background future but random cpu scheduler delays are highly unpredictable
    nanoDelayUntil(startTime_ns)
    promise.complete(Try(f()))
  }

  case class PeriodicTaskRunnable[U](
    task: () => U,
    initialDelay: Duration,
    period: Duration
  )(implicit scheduledExecutionContext: ScheduledExecutionContext) extends Runnable {
    val initialDelay_ns = initialDelay.toNanos
    val period_ns = period.toNanos

    val _nextEvent_ns = new java.util.concurrent.atomic.AtomicLong(System.nanoTime() + initialDelay_ns)
    def nextEvent_ns = _nextEvent_ns.get

    override def run() = {
      try {
        nanoDelayUntil(nextEvent_ns)
        task()
        _nextEvent_ns.getAndSet(System.nanoTime() + period_ns)
      } catch {
        case ex:Exception =>
          scheduledExecutionContext.reportFailure(ex)
          throw ex
      }
    }
  }

  case class PeriodicTaskImpl[U](
    runnable: PeriodicTaskRunnable[U],
    javaScheduledFuture: java.util.concurrent.ScheduledFuture[Unit]
  ) extends PeriodicTask {
    val onCancel = Latch()
    override def nextEvent_ns = runnable.nextEvent_ns
    override def initialDelay = runnable.initialDelay
    override def period = runnable.period

    override def cancel() = {
      onCancel.set()
      javaScheduledFuture.cancel(false)
    }

  }
}

case class ScheduledExecutionContextImpl(
  delegate: ScheduledExecutorService
)(implicit
  executionContext: ExecutionContext
) extends ScheduledExecutionContext {
  import ScheduledExecutionContextImpl._

  def schedule[A](delay: Duration)(f: () => A) : DelayedFuture[A] = {
    val delay_ns = delay.toNanos
    // Below a certain threshold, only a computed percentage of the requested delay is scheduled. The rest is
    // performed in a spin wait loop. This trades cpu time for precision which fixes large delay errors in
    // short (micros) delays
    val scheduledDelay_ns = calcScheduledDelay_ns(delay_ns)
    if (scheduledDelay_ns > 0) {
      ScheduledDelayedFutureImpl(f, delay, scheduledDelay_ns, delegate)
    } else {
      // Note: this trades cpu time for more precise delay
      SpinDelayedFutureImpl(f, delay)
    }
  }

  def scheduleAtFixedRate[U](initialDelay: Duration, period: Duration)(task: () => U) : PeriodicTask = {
    val runnable = PeriodicTaskRunnable(
      task,
      initialDelay,
      period
    )(this)

    val javaScheduledFuture =
      delegate.scheduleAtFixedRate(
        runnable,
        initialDelay.toNanos,
        period.toNanos,
        TimeUnit.NANOSECONDS
      )


    PeriodicTaskImpl(runnable, javaScheduledFuture.asInstanceOf[ScheduledFuture[Unit]])
  }

  override def reportFailure(cause: Throwable) = executionContext.reportFailure(cause)
}