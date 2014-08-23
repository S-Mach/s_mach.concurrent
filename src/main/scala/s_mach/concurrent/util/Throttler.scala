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

import s_mach.concurrent.ScheduledExecutionContext

import scala.util.Try
import java.util.concurrent.{TimeUnit, ScheduledExecutorService}
import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.concurrent.duration._

/**
 * A trait that ensures a series of tasks run no faster than the throttle setting. Callers schedule tasks by calling the
 * run method. If at least throttle setting nanoseconds have passed since the last task completed, the supplied function
 * is immediately executed, otherwise it is scheduled for execution once throttle nanoseconds have expired. Tasks
 * scheduled at the same time for later execution will be executed in a FIFO manner. Throttler guarantees that all tasks
 * will have at least throttle nanoseconds separating their completion. However, it can not guarantee that the elapsed
 * time between tasks will not be greater than the throttle setting.
 *
 * Mean timing error for throttle setting in default Throttler implementation:
 * 100ms 0.04% error
 * 10ms 0.4% error
 * 1ms 3% error
 * 100us 13% error
 * 10us 15% error
 * 5us 15% error
 *
 * Note: throttle settings below 5us produce unstable results
*/
trait Throttler extends ThrottleControl {
  /** @return a Future that completes once at least throttle_ns nanoseconds have expired since the last task AND task
    * completes */
  def run[X](task: () => X)(implicit ec:ExecutionContext) : Future[X]
}

object Throttler {

  class ThrottleImpl(__throttle_ns: Long)(implicit ec: ExecutionContext, scheduledExecutionContext: ScheduledExecutionContext) extends Throttler {

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

  def apply(throttle_ns: Long)(implicit ec: ExecutionContext, scheduledExecutionContext: ScheduledExecutionContext) : Throttler = new ThrottleImpl(throttle_ns)
}