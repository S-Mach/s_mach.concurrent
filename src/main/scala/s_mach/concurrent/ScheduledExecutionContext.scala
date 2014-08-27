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

import java.util.concurrent.{ScheduledFuture, TimeUnit, ScheduledExecutorService, ThreadFactory, Executors}
import s_mach.concurrent.impl.DelegatedFuture
import s_mach.concurrent.util.Latch
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import s_mach.concurrent.impl.FutureOps._

trait ScheduledExecutionContext {
  /**
   * Create a DelayedFuture that executes the supplied function after the given delay
   *
   * @param delay the time from now to delay execution
   * @param f the function to execute
   * @return a DelayedFuture that can be used to extract result or cancel (only before it has been started)
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   */
  def schedule[A](delay: Duration)(f: () => A) : DelayedFuture[A]

  /**
   * Creates a PeriodicTask that executes first after the given initial delay, and subsequently with the given period.
   * PeriodicTask may stopped using the cancel method or will end automatically on should a failure occur while
   * processing the task. If any execution of this task takes longer than its period, then subsequent executions may
   * start late, but will not concurrently execute.
   *
   * @param initialDelay the time to delay first execution
   * @param period the period between successive executions
   * @param task the task to execute
   * @return a PeriodicTask
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   * @throws IllegalArgumentException if period less than or equal to zero
   */
  def scheduleAtFixedRate[U](initialDelay: Duration, period: Duration)(task: () => U) : PeriodicTask

  /**
   * Report a failure. Used to report failures during periodic tasks
   * @param cause
   */
  def reportFailure(cause: Throwable) : Unit
}

object ScheduledExecutionContext {
//  val ACCEPTABLE_DELAY_ERROR_PERCENT = 0.1
//  val lock = new Object
//  val minScheduledDelay_ns = new java.util.concurrent.atomic.AtomicLong(0)
//  val minScheduledDelayDecay_ns = new java.util.concurrent.atomic.AtomicLong(0)
  // TODO: these values were tuned on my specific hardware config and should instead be calibrated at startup and adjusted at runtime
  val MIN_SCHEDULED_DELAY_NS = 1000000
  val MIN_SPIN_DELAY_NS = 300
  val SPIN_DELAY_NS = 5000

//  def reportSpinDelayError(targetDelay_ns: Long, startTime_ns: Long, eventTime_ns: Long) = ???
//  def reportScheduledDelayError(targetDelay_ns: Long, startTime_ns: Long, eventTime_ns: Long) = {
//    val actualDelay_ns = eventTime_ns - startTime_ns
//    // If there was more than a 10% error then set then adjust minimum scheduled delay 50% closer to failure
//    if (actualDelay_ns > targetDelay_ns * (1.0 + ACCEPTABLE_DELAY_ERROR_PERCENT)) {
//      lock.synchronized {
////        val error_ns = targetDelay_ns - minScheduledDelay_ns.get
////        val localMinScheduledDelay_ns = minScheduledDelay_ns.get
//        val newMinScheduledDelay_ns = minScheduledDelay_ns.addAndGet((targetDelay_ns * 1.25).toLong)
//        minScheduledDelayDecay_ns.set(Math.min(-1,-(newMinScheduledDelay_ns * 0.001).toLong))
//        println(s"ERRCORRECT ${minScheduledDelay_ns.get} ${minScheduledDelayDecay_ns.get}")
//      }
//    } else {
//      if (minScheduledDelay_ns.get > 0) println("here")
//      if(minScheduledDelayDecay_ns.get <0 ) println(s"before ${minScheduledDelay_ns.get}")
//      // Success - decay minScheduledDelay_ns
//      minScheduledDelay_ns.addAndGet(minScheduledDelayDecay_ns.get)
//      if(minScheduledDelayDecay_ns.get <0 ) println(s"after ${minScheduledDelay_ns.get}")
//    }
//  }

  def nanoDelayUntil(nextEvent_ns: Long): Unit = {
    val done_ns = nextEvent_ns - MIN_SPIN_DELAY_NS
    while(System.nanoTime() < done_ns) { }
  }

  case class ScheduledDelayedFutureImpl[A](
    f: () => A,
    delay: Duration,
    scheduledExecutorService: ScheduledExecutorService
  )(implicit ec:ExecutionContext) extends DelayedFuture[A] with DelegatedFuture[A] {
    val delay_ns = delay.toNanos
    val startTime_ns = System.nanoTime() + delay_ns
    val scheduledDelay_ns = delay_ns - SPIN_DELAY_NS
    val promise = Promise[A]()

    val javaScheduledFuture =
      scheduledExecutorService.schedule(
        new Runnable {
          override def run() = {
//            Future { reportScheduledDelayError(delay_ns, startTime_ns, System.nanoTime()) }.background
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
    nanoSpinDelay(delay.toNanos - MIN_SPIN_DELAY_NS)
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
  

  case class ScheduledExecutionContextImpl(delegate: ScheduledExecutorService)(implicit executionContext: ExecutionContext) extends ScheduledExecutionContext {
    def schedule[A](delay: Duration)(f: () => A) : DelayedFuture[A] = {
      val delay_ns = delay.toNanos
      // Below a certain threshold, the scheduled executor is no longer capable of producing accurate delays
      if (delay_ns > MIN_SCHEDULED_DELAY_NS) {
        // Schedule delay early for more precise delays (any remaining delay will be accomplished through spin delay)
        ScheduledDelayedFutureImpl(f, delay, delegate)
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

  def fromExecutor(scheduledExecutorService: ScheduledExecutorService)(implicit executionContext: ExecutionContext) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(scheduledExecutorService)
  def apply(corePoolSize: Int, threadFactory: ThreadFactory)(implicit executionContext: ExecutionContext) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(Executors.newScheduledThreadPool(corePoolSize, threadFactory))
  def apply(corePoolSize: Int)(implicit executionContext: ExecutionContext) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(Executors.newScheduledThreadPool(corePoolSize))
}