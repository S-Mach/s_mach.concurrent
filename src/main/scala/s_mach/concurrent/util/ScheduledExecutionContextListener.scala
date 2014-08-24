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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import s_mach.concurrent.{PeriodicTask, DelayedFuture, ScheduledExecutionContext}

/**
 * An immutable ScheduleAtFixedRatedExecutorService wrapper that allows listening to the ScheduleAtFixedRatedExecutorService events of the delegate.
 */
class ScheduledExecutionContextListener(
  delegate: ScheduledExecutionContext,
  _onSchedule: NotifiableListener[Any, (Duration, () => Any)] = NotifiableListener(),
  _onScheduleAtFixedRate: NotifiableListener[Any, (Duration, Duration, () => Any)] = NotifiableListener(),
  _onStart: NotifiableListener[Any, () => Any] = NotifiableListener(),
  _onComplete: NotifiableListener[Any, () => Any] = NotifiableListener(),
  _onFail: NotifiableListener[Any, Throwable] = NotifiableListener()
)(implicit ec:ExecutionContext) extends ScheduledExecutionContext {

  override def schedule[A](delay: Duration)(f: () => A): DelayedFuture[A] = {
    _onSchedule.notifyListeners((delay, f))
    delegate.schedule(delay) { () =>
      _onStart.notifyListeners(f)
      val retv = f()
      _onComplete.notifyListeners(f)
      retv
    }
  }

  override def scheduleAtFixedRate[U](initialDelay: Duration, period: Duration)(task: () => U): PeriodicTask = {
    _onScheduleAtFixedRate.notifyListeners((initialDelay, period, task))
    _onStart.notifyListeners(task)
    val periodicTask = delegate.scheduleAtFixedRate(initialDelay, period)(task)
    periodicTask.onCancel.onSet { () =>
      _onComplete.notifyListeners(task)
    }
    periodicTask
  }

  override def reportFailure(cause: Throwable) = {
    _onFail.notifyListeners(cause)
    delegate.reportFailure(cause)
  }

  def onSchedule = _onSchedule.lens(l => copy(_onSchedule = l))
  def onScheduleAtFixedRate = _onScheduleAtFixedRate.lens(l => copy(_onScheduleAtFixedRate = l))
  def onStart = _onStart.lens(l => copy(_onStart = l))
  def onComplete = _onComplete.lens(l => copy(_onComplete = l))
  def onFail = _onFail.lens(l => copy(_onFail = l))

  protected def copy(
    delegate: ScheduledExecutionContext = delegate,
     _onSchedule: NotifiableListener[Any, (Duration, () => Any)] = _onSchedule,
    _onScheduleAtFixedRate: NotifiableListener[Any, (Duration, Duration, () => Any)] = _onScheduleAtFixedRate,
    _onStart: NotifiableListener[Any, () => Any] = _onStart,
    _onComplete: NotifiableListener[Any, () => Any] = _onComplete,
    _onFail: NotifiableListener[Any, Throwable] = _onFail
  ) = new ScheduledExecutionContextListener(
    delegate = delegate,
    _onSchedule = _onSchedule,
    _onScheduleAtFixedRate = _onScheduleAtFixedRate,
    _onStart = _onStart,
    _onComplete = _onComplete,
    _onFail = _onFail
  )

}

object ScheduledExecutionContextListener {
  def apply(delegate: ScheduledExecutionContext)(implicit ec:ExecutionContext) : ScheduledExecutionContextListener = new ScheduledExecutionContextListener(delegate)
}