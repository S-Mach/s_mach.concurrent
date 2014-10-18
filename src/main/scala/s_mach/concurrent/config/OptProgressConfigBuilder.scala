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
package s_mach.concurrent.config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util._

/**
 * A trait for an immutable builder of OptProgressConfig. Callers may set the
 * optional progress reporter by calling one of the progress methods. If no
 * progress reporting method is ever called then the optional progress function
 * is left unset.
 * @tparam MDT most derived type
 */
trait OptProgressConfigBuilder[MDT <: OptProgressConfigBuilder[MDT]] {

  /** The optional total number of operations as set by derived type */
  def optTotal : Option[Int]

  /**
   * Set the optional progress reporting function.
   * @param reporter a function that accepts the number of completed operations
   *                 (typically this is 1) as they occur
   * @return a copy of the builder with the new setting
   */
  def progress(reporter: TaskEventListener)(implicit ec:ExecutionContext) : MDT

  /**
   * Set the progress reporting function
   * @param report a function that accepts the number of operations completed
   *               and the total number of operations (or 0 if the total is
   *               unknown)
   * @return a copy of the builder with the new setting
   */
  def progress(report: Progress => Unit)(implicit ec:ExecutionContext) : MDT =
    progress(SimpleProgressReporter(optTotal, report))

  /**
   * Set the progress reporting function to periodically report progress
   * @param reportInterval the interval to report progress
   * @param report a function that accepts the number of operations completed
   *               and the total number of operations (or 0 if the total is
   *               unknown)
   * @return a copy of the builder with the new setting
   */
  def progress(reportInterval: Duration)(report: Progress => Unit)(implicit
    executionContext: ExecutionContext,
    scheduledExecutionContext: ScheduledExecutionContext
  ) : MDT = {
    progress(PeriodicProgressReporter(optTotal, reportInterval, report))
  }

  /** @return a ProgressConfig with the optional progress function */
  def build() : OptProgressConfig
}

