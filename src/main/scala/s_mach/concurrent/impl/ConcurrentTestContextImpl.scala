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
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util.{Timer, SerializationSchedule, ConcurrentTestContext}

class ConcurrentTestContextImpl()(implicit
    ec: ExecutionContext,
    sec: ScheduledExecutionContext
  ) extends ConcurrentTestContext {
  val _activeExecutionCount = new java.util.concurrent.atomic.AtomicInteger(0)
  override def activeExecutionCount = _activeExecutionCount.get

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
      }
      .onComplete.add(1) { (_,_) =>
        _activeExecutionCount.decrementAndGet()
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
