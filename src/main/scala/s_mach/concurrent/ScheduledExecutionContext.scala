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

import java.util.concurrent.{ScheduledExecutorService, ThreadFactory, Executors}
import scala.concurrent._
import scala.concurrent.duration.Duration
import s_mach.concurrent.impl.ScheduledExecutionContextImpl

/**
 * A trait for scheduling delayed or periodic tasks
 */
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
  def fromExecutor(
    scheduledExecutorService: ScheduledExecutorService
  )(implicit
    executionContext: ExecutionContext
  ) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(scheduledExecutorService)

  def apply(
    corePoolSize: Int,
    threadFactory: ThreadFactory
  )(implicit
    executionContext: ExecutionContext
  ) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(Executors.newScheduledThreadPool(corePoolSize, threadFactory))

  def apply(corePoolSize: Int)(implicit executionContext: ExecutionContext) : ScheduledExecutionContext =
    ScheduledExecutionContextImpl(Executors.newScheduledThreadPool(corePoolSize))
}