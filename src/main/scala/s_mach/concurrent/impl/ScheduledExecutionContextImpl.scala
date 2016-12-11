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
package s_mach.concurrent.impl

import java.util.concurrent.{ScheduledFuture, TimeUnit, ScheduledExecutorService}
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try
import s_mach.concurrent._
import s_mach.concurrent.util.{AtomicFSM, Latch, DelegatedFuture}

object ScheduledExecutionContextImpl {
  class ScheduledDelayedFutureImpl[A](
    task: () => A,
    val delay: FiniteDuration,
    scheduledExecutorService: ScheduledExecutorService
  )(implicit
    ec:ExecutionContext
  ) extends DelayedFuture[A] with DelegatedFuture[A] {

    val delay_ns = delay.toNanos
    val startTime_ns = System.nanoTime + delay_ns
    val deferredPromise = Promise[Future[A]]()
    val delegatePromise = Promise[A]()

    val javaScheduledFuture =
      scheduledExecutorService.schedule(
        new Runnable {
          override def run() = {
            // Signal that the future has started
            deferredPromise.success(delegate)
            delegatePromise.complete(Try(task()))
            ()
          }
        },
        delay_ns,
        TimeUnit.NANOSECONDS
      )

    val delegate = delegatePromise.future

    val deferred = deferredPromise.future
  }
  class CancellableScheduledDelayedFutureImpl[A](
    task: () => A,
    fallback: => A,
    delay: FiniteDuration,
    scheduledExecutorService: ScheduledExecutorService
  )(implicit
    ec:ExecutionContext
  ) extends ScheduledDelayedFutureImpl[A](task, delay, scheduledExecutorService)
  with CancellableDelayedFuture[A] {
    val wasCancelled = Latch()
    
    override def isCancelled = wasCancelled.isSet

    override def canCancel = deferredPromise.isCompleted == false

    override def cancel() = {
      if(canCancel) {
        // Attempt to cancel
        if(javaScheduledFuture.cancel(false)) {
          // If cancel was successful, still need to make this future complete
          // with something in case there waiting listeners
          // Signal that the future has started
          deferredPromise.success(delegate)
          delegatePromise.success(fallback)
          wasCancelled.set()
          true
        } else {
          false
        }
      } else {
        // Future has already started
        false
      }
    }

  }

  class PeriodicTaskImpl[U](
    task: () => U,
    val initialDelay: FiniteDuration,
    val period: FiniteDuration,
    initiallyPaused: Boolean,
    scheduledExecutorService: ScheduledExecutorService,
    reportFailure: Throwable => Unit
  ) extends PeriodicTask { self =>
    import PeriodicTask._

    val initialDelay_ns = initialDelay.toNanos
    val period_ns = period.toNanos
    val onCancel = Latch()


    def s0 = if(initiallyPaused == false) {
      val r = RunningImpl(initialDelay_ns)
      r.start()
      r
    } else {
      PausedImpl(0)
    }

    def state = _state.get

    val _state = new AtomicFSM[State](s0)

    case class RunningImpl(initialDelay_ns: Long) extends Running {
      val _nextEvent_ns = new java.util.concurrent.atomic.AtomicLong(
        System.nanoTime() + initialDelay_ns
      )
      def nextEvent_ns = _nextEvent_ns.get

      val runnable = new Runnable {
        override def run() = {
          try {
            task()
            _nextEvent_ns.getAndSet(System.nanoTime() + period_ns)
            ()
          } catch {
            case t:Throwable =>
              reportFailure(t)
              throw t
          }
        }
      }

      val _javaScheduledFuture = Promise[ScheduledFuture[_]]()
      def javaScheduledFuture = _javaScheduledFuture.future.get

      def start() = {
        _javaScheduledFuture.success {
          scheduledExecutorService.scheduleAtFixedRate(
            runnable,
            initialDelay_ns,
            period_ns,
            TimeUnit.NANOSECONDS
          )
        }
      }

      override def pause(): Boolean = {
        _state.fold(
          transition = {
            case current:RunningImpl =>
              (PausedImpl(current.nextEvent_ns),true)
            case s => (s,false)
          },
          onTransition = {
            case (r:RunningImpl,p:Paused) =>
              r.javaScheduledFuture.cancel(false)
              ()
          }
        )
      }
    }

    case class PausedImpl(nextEvent_ns: Long) extends Paused {
      override def resume(): Boolean = {
        _state.fold(
          transition = {
            case current:PausedImpl =>
              (
                RunningImpl(Math.max(0,current.nextEvent_ns - System.nanoTime())),
                true
              )
            case s => (s,false)
          },
          onTransition = {
            case (p:Paused,r:RunningImpl) =>
              r.start()
              ()
          }
        )
      }
    }

    override def nextEvent_ns: Long = _state.get match {
      case state:RunningImpl => state.nextEvent_ns
      case _ => Long.MaxValue
    }

    override def cancel() = {
      _state.fold(
        transition = {
          case _:Running | _:Paused =>
            (Cancelled, true)
          case _ => (Cancelled, false)
        },
        onTransition = {
          case (r:RunningImpl,Cancelled) =>
            r.javaScheduledFuture.cancel(false)
            onCancel.set()
          case (p:Paused,Cancelled) =>
            onCancel.set()
        }
      )
    }

    override def toString = s"PeriodicTaskImpl(state=$state)"
  }
}

case class ScheduledExecutionContextImpl(
  delegate: ScheduledExecutorService
)(implicit
  executionContext: ExecutionContext
) extends ScheduledExecutionContext {
  import ScheduledExecutionContextImpl._

  def schedule[A](delay: FiniteDuration)(task: => A) : DelayedFuture[A] = {
    new ScheduledDelayedFutureImpl(
      task = { () => task },
      delay = delay,
      scheduledExecutorService = delegate
    )
  }

  override def scheduleCancellable[A](
    delay: FiniteDuration,
    fallback: => A
  )(
    task: => A
  ): CancellableDelayedFuture[A] = {
    new CancellableScheduledDelayedFutureImpl(
      task = { () => task },
      fallback = fallback,
      delay = delay,
      scheduledExecutorService = delegate
    )
  }

  def scheduleAtFixedRate[U](
    initialDelay: FiniteDuration,
    period: FiniteDuration,
    paused: Boolean = false
  )(task: () => U) : PeriodicTask = {
    new PeriodicTaskImpl(
      task = task,
      initialDelay = initialDelay,
      period = period,
      initiallyPaused = paused,
      scheduledExecutorService = delegate,
      reportFailure = { t => reportFailure(t) }
    )
  }

  def reportFailure(cause: Throwable) = executionContext.reportFailure(cause)
}