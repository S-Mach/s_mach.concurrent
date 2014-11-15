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
import scala.concurrent.duration.FiniteDuration
import s_mach.concurrent.{PeriodicTask, ScheduledExecutionContext}
import s_mach.concurrent.util.{Progress, PeriodicProgressReporter}

class PeriodicProgressReporterImpl(
  optTotal: Option[Int],
  val reportInterval: FiniteDuration,
  report: Progress => Unit
)(implicit
  executionContext: ExecutionContext,
  scheduledExecutionContext: ScheduledExecutionContext
) extends PeriodicProgressReporter {
  val totalSoFar = new java.util.concurrent.atomic.AtomicInteger(0)

  val lock = new Object
  var startTime_ns = 0l
  var lastReport_ns = 0l
  var reporter : Option[PeriodicTask] = None

  override def onStartTask() = {
    lock.synchronized {
      reporter = Some(
        scheduledExecutionContext.scheduleAtFixedRate(
          reportInterval,
          reportInterval
        ) { () =>
          doReport(totalSoFar.get)
        }
      )
      val now = System.nanoTime()
      startTime_ns = now
      lastReport_ns = now
      report(Progress(0, optTotal, startTime_ns))
    }
  }

  override def onCompleteTask() = {
    lock.synchronized {
      require(reporter != None)
      require(optTotal.forall(_ == totalSoFar.get))

      report(Progress(totalSoFar.get, optTotal, startTime_ns))
      reporter.get.cancel()
      reporter = None
    }
  }

  override def onStartStep(stepId: Int) = { }

  override def onCompleteStep(stepId: Int) = totalSoFar.addAndGet(1)

  def doReport(localTotalSoFar: Int) {
    lock.synchronized {
      // Note: is possible for a report to be queued on the lock while onEnd is
      // in progress
      if(reporter != None) {
        lastReport_ns = System.nanoTime()
        report(Progress(localTotalSoFar, optTotal, startTime_ns))
      }

    }
  }

}
