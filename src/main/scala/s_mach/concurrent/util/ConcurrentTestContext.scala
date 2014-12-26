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
import s_mach.concurrent.impl.ConcurrentTestContextImpl
import s_mach.concurrent.ScheduledExecutionContext

/**
 * A context for testing concurrent code that provides:
 * 1) an ExecutionContext that keeps track of the number of active and completed
 * Runnables
 * 2) a Timer for tracking elapsed duration since start of test
 * 3) a SerializationSchedule for detecting order of execution of events
 * 4) a precision delay function that accumulates any delay error
 */
trait ConcurrentTestContext extends
  ExecutionContext with
  ScheduledExecutionContext {
  /** @return the current number of active Runnables and scheduled tasks being
    *         processed */
  def activeExecutionCount: Int

  /** @return the SerializationSchedule for the context */
  implicit def sched: SerializationSchedule[String]

  /** precisely delay for a period of time in nanoseconds. If the elapsed time
    * differs from the requested the delay, the error in delay is accumulated in
    * delayError_ns */
  def delay(delay_ns: Long) : Unit
  /** @return the accumulated delay error */
  def delayError_ns : Long

  /** Sleep until the active execution count is equal to or less than the
    * specified value */
  def waitForActiveExecutionCount(_activeRunnableCount: Int) {
    while(this.activeExecutionCount > _activeRunnableCount) {
      Thread.sleep(1)
    }
  }
}

object ConcurrentTestContext {
  def apply()(implicit
    ec: ExecutionContext,
    sec: ScheduledExecutionContext
  ) : ConcurrentTestContext = new ConcurrentTestContextImpl()
}

