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

import scala.concurrent.duration.Duration
import s_mach.concurrent.util.Barrier

/**
 * A trait for a task that after the initial delay expires, is repeatedly
 * started with a specified period in the background. This will continue until
 * the task is cancelled or a failure occurs.
 */
trait PeriodicTask {
  /** @return the time stamp in nanoseconds when the next task will be started
    * */
  def nextEvent_ns : Long
  /** @return the delay before initially starting the task */
  def initialDelay : Duration
  /** @return the recurring delay between the task executions */
  def period : Duration
  /** @return TRUE if the task was cancelled FALSE if the task was already
    *         cancelled */
  def cancel() : Boolean
  /** @return a barrier that is set once the task is cancelled */
  def onCancel : Barrier
}