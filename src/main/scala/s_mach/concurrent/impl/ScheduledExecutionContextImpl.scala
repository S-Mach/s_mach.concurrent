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

import java.util.concurrent.{TimeUnit, ScheduledExecutorService}
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try
import s_mach.concurrent._
import s_mach.concurrent.util.{Latch, DelegatedFuture}

object ScheduledExecutionContextImpl {
  case class ScheduledDelayedFutureImpl[A](
    task: () => A,
    delay: FiniteDuration,
    scheduledExecutorService: ScheduledExecutorService
  )(implicit
    ec:ExecutionContext
  ) extends DelayedFuture[A] with DelegatedFuture[A] {

    val delay_ns = delay.toNanos
    val startTime_ns = System.nanoTime + delay_ns
    val futPromise = Promise[Future[A]]()
    val promise = Promise[A]()

    val javaScheduledFuture =
      scheduledExecutorService.schedule(
        new Runnable {
          override def run() = {
            futPromise.success(delegate)
            promise.complete(Try(task()))
          }
        },
        delay_ns,
        TimeUnit.NANOSECONDS
      )

    val delegate = promise.future

    val deferred = futPromise.future
  }

  case class PeriodicTaskImpl[U](
    task: () => U,
    initialDelay: FiniteDuration,
    period: FiniteDuration,
    scheduledExecutorService: ScheduledExecutorService,
    reportFailure: Throwable => Unit
  ) extends PeriodicTask {

    val initialDelay_ns = initialDelay.toNanos
    val period_ns = period.toNanos

    val _nextEvent_ns = new java.util.concurrent.atomic.AtomicLong(
      System.nanoTime() + initialDelay_ns
    )
    def nextEvent_ns = _nextEvent_ns.get

    val runnable = new Runnable {
      override def run() = {
        try {
          task()
          _nextEvent_ns.getAndSet(System.nanoTime() + period_ns)
        } catch {
          case t:Throwable =>
            reportFailure(t)
            throw t
        }
      }
    }

    val javaScheduledFuture =
      scheduledExecutorService.scheduleAtFixedRate(
        runnable,
        initialDelay_ns,
        period_ns,
        TimeUnit.NANOSECONDS
      )

    val onCancel = Latch()

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

  def schedule[A](delay: FiniteDuration)(task: => A) : DelayedFuture[A] = {
    ScheduledDelayedFutureImpl(
      task = { () => task },
      delay = delay,
      scheduledExecutorService = delegate
    )
  }

  def scheduleAtFixedRate[U](
    initialDelay: FiniteDuration,
    period: FiniteDuration
  )(task: () => U) : PeriodicTask = {
    PeriodicTaskImpl(
      task = task,
      initialDelay = initialDelay,
      period = period,
      scheduledExecutorService = delegate,
      reportFailure = { t => reportFailure(t) }
    )
  }

  def reportFailure(cause: Throwable) = executionContext.reportFailure(cause)
}