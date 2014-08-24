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

import java.util.concurrent._

import s_mach.concurrent.util.Latch

import scala.concurrent.{Promise, CanAwait, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try

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
  def ensureDelay(nextEvent_ns: Long): Unit = {
    val remaining_ms = (nextEvent_ns - System.nanoTime()).nanos.toMillis
    // Scheduled executor service seems to consistently be early sometimes as many as 5-10 ms early though it dips as low as 71 us
    if(remaining_ms > 3) {
      Thread.sleep(remaining_ms)
    }
    while(System.nanoTime() < nextEvent_ns) { }
  }

  case class DelayedRunnable[A](
    f: () => A,
    delay: Duration
  )(implicit executionContext: ExecutionContext) extends Runnable {
    val nextEvent_ns = System.nanoTime() + delay.toNanos

    val promise = Promise[A]()

    override def run() = {
      try {
        ensureDelay(nextEvent_ns)
        promise.complete(Try(f()))

      } catch {
        case ex:Exception =>
          executionContext.reportFailure(ex)
          throw ex
      }
    }
  }

  case class DelayedFutureImpl[A](
    runnable: DelayedRunnable[A],
    javaScheduledFuture: java.util.concurrent.ScheduledFuture[Unit]
  ) extends DelayedFuture[A] {
    val delegate = runnable.promise.future

    override def startTime_ns = runnable.nextEvent_ns
    override def delay = runnable.delay
    override def cancel() = javaScheduledFuture.cancel(false)

    override def onComplete[U](f: (Try[A]) => U)(implicit executor: ExecutionContext): Unit = delegate.onComplete(f)
    override def isCompleted: Boolean = delegate.isCompleted
    override def value: Option[Try[A]] = delegate.value
    @scala.throws[Exception](classOf[Exception])
    override def result(atMost: Duration)(implicit permit: CanAwait): A = delegate.result(atMost)
    @scala.throws[InterruptedException](classOf[InterruptedException])
    @scala.throws[TimeoutException](classOf[TimeoutException])
    override def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
      delegate.ready(atMost)
      this
    }
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
        ensureDelay(nextEvent_ns)
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
      val runnable = DelayedRunnable(
        f,
        delay
      )

      val javaScheduledFuture =
        delegate.schedule(
          runnable,
          delay.toNanos,
          TimeUnit.NANOSECONDS
        )

      DelayedFutureImpl(runnable, javaScheduledFuture.asInstanceOf[ScheduledFuture[Unit]])
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