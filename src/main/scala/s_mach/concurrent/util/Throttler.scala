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

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.{DeferredFuture, ScheduledExecutionContext}
import s_mach.concurrent.impl.ThrottlerImpl

/**
 * A trait that ensures a series of tasks run no faster than the throttle
 * setting. Callers schedule tasks by calling the run method. If at least
 * throttle setting nanoseconds have passed since the last task completed, the
 * supplied function is immediately executed, otherwise it is scheduled for
 * execution once throttle nanoseconds have expired. Tasks scheduled at the same
 * time for later execution will be executed in a FIFO manner. Throttler
 * guarantees that all tasks will have at least throttle nanoseconds separating
 * their completion. However, it can not guarantee that the elapsed time between
 * tasks will not be greater than the throttle setting.
*/
trait Throttler extends ThrottleControl {
  /** @return a Future that completes once at least throttle_ns nanoseconds have
    *         expired since the last task AND task completes */
  def run[X](
    task: => Future[X]
  )(implicit ec:ExecutionContext) : DeferredFuture[X]
}

object Throttler {
  def apply(
    throttle_ns: Long
  )(implicit
    scheduledExecutionContext: ScheduledExecutionContext
  ) : Throttler = new ThrottlerImpl(throttle_ns)
}