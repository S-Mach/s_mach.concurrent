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

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util.{Latch, Barrier, Throttler}
import s_mach.concurrent._

class ThrottlerImpl(
  __throttle_ns: Long
)(implicit
  scheduledExecutionContext: ScheduledExecutionContext
) extends Throttler {

  private[this] val _throttle_ns =
    new java.util.concurrent.atomic.AtomicLong(__throttle_ns)

  override def throttle_ns = _throttle_ns.get

  override def throttle_ns(throttle_ns: Long) = _throttle_ns.getAndSet(throttle_ns)

  override def adjustThrottle_ns(adjust_ns: Long) = _throttle_ns.getAndAdd(adjust_ns)

  private[this] val lock = new Object
  private[this] var lastEvent = Barrier.set

  def run[X](f: => Future[X])(implicit ec: ExecutionContext): DeferredFuture[X] = {
    lock.synchronized {
      val latch = Latch()
      val retv = DeferredFuture {
        lastEvent happensBefore {
          scheduledExecutionContext.schedule(throttle_ns.nanos) {
            f sideEffect latch.set()
          }
        }
      }
      lastEvent = latch
      retv
    }
  }
}

