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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import s_mach.concurrent._
import s_mach.concurrent.util.{SerializationSchedule, ConcurrentTestContext}

class ConcurrentTestContextImpl()(implicit
    ec: ExecutionContext,
    sec: ScheduledExecutionContext
  ) extends ConcurrentTestContext {
  val _activeExecutionCount = new java.util.concurrent.atomic.AtomicInteger(0)
  override def activeExecutionCount = _activeExecutionCount.get

  override def reportFailure(cause: Throwable) = ec.reportFailure(cause)
  override def execute(runnable: Runnable) = {
    _activeExecutionCount.incrementAndGet()
    ec.execute(new Runnable {
      override def run() = {
        runnable.run()
        _activeExecutionCount.decrementAndGet()
      }
    })
  }

  override def scheduleAtFixedRate[U](initialDelay: Duration, period: Duration)(task: () => U) = {
    _activeExecutionCount.incrementAndGet()
    val retv = sec.scheduleAtFixedRate(initialDelay, period)(task)
    retv.onCancel.onSet {
      _activeExecutionCount.decrementAndGet()
    }
    retv
  }
  override def schedule[A](delay: Duration)(f: => A) = {
    _activeExecutionCount.incrementAndGet()
    val retv = sec.schedule(delay)(f)
    retv.sideEffect(_activeExecutionCount.decrementAndGet()).background
    retv
  }


  // Run first tests with no delay to compute avg base line
  override implicit val sched = SerializationSchedule[String]()

  val _delayError_ns = new java.util.concurrent.atomic.AtomicLong(0)

  override def delayError_ns = _delayError_ns.get

  override def delay(delay_ns: Long) = {
    val err_ns = FutureOps.nanoSpinDelay(delay_ns)
    _delayError_ns.addAndGet(err_ns)
  }
}
