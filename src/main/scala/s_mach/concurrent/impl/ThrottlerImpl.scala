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

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.Try
import scala.concurrent.duration._
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util.{Latch, Barrier, Throttler}

class ThrottlerImpl(
  __throttle_ns: Long
)(implicit
  scheduledExecutionContext: ScheduledExecutionContext
) extends Throttler {

  private[this] val _throttle_ns = new java.util.concurrent.atomic.AtomicLong(__throttle_ns)

  override def throttle_ns = _throttle_ns.get

  override def throttle_ns(throttle_ns: Long) = _throttle_ns.getAndSet(throttle_ns)

  override def adjustThrottle_ns(adjust_ns: Long) = _throttle_ns.getAndAdd(adjust_ns)

  private[this] val lock = new Object
  private[this] var lastEvent = Barrier.set
  private[this] var lastEvent_ns = System.nanoTime() - throttle_ns
  private[this] var busyWaitTune_ns = 200000

  def run[X](f: () => X)(implicit ec: ExecutionContext): Future[X] = {
    lock.synchronized {
      val promise = Promise[X]()
      val latch = Latch()
      lastEvent onSet { () =>
        // Note: this will always run serially
        val expected_ns = lastEvent_ns + throttle_ns

        def fireEvent() {
          var i = 0
          while (expected_ns > System.nanoTime()) {
            i = i + 1
          }
          // Note: tuned these constants using ThrottlerTest. Want a small amount of busy wait but not too much.
          // Also, when tuning don't want to over-correct or under-correct.
          if (i == 0) {
            busyWaitTune_ns += 10000
          } else if (i > 500) {
            busyWaitTune_ns -= Math.min(busyWaitTune_ns, (i / 100) * 2000)
          }
          // TODO: measure actual elapsed time and correct for consistent overages to allow for more accuracy in the lower throttle ranges
          promise.complete(Try(f()))
          lastEvent_ns = System.nanoTime()
          latch.set()
        }

        // Note: busy wait a certain amount of the delay for better accuracy. Don't want to busy wait all the delay
        // since it consumes a thread to do so
        val delay_ns = (expected_ns - System.nanoTime()) - busyWaitTune_ns
        // Scheduled executor service is inaccurate beyond a certain minimum delay value
        if (delay_ns > 0) {
          scheduledExecutionContext.schedule(delay_ns.nanos) { () => fireEvent() }
        } else {
          fireEvent()
        }
      }
      lastEvent = latch
      promise.future
    }
  }
}

