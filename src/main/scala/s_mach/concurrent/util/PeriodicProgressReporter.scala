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
package s_mach.concurrent.util

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.impl.PeriodicProgressReporterImpl

/**
 * A trait for a progress reporter that reports progress only at the specified
 * report interval
 */
trait PeriodicProgressReporter extends TaskEventListener {
  def reportInterval: FiniteDuration
}

object PeriodicProgressReporter {
  def apply(
    optTotal: Option[Int],
    reportInterval: FiniteDuration,
    report: Progress => Unit
  )(implicit
    executionContext: ExecutionContext,
    scheduledExecutionContext: ScheduledExecutionContext
  ) : PeriodicProgressReporter = new PeriodicProgressReporterImpl(
    optTotal = optTotal,
    reportInterval = reportInterval,
    report = report
  )
}
