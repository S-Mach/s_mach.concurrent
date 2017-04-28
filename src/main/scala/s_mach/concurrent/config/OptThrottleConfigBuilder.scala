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
         .t1i .,::;;; ;1tt        Copyright (c) 2017 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.config

import scala.concurrent.duration.Duration
import s_mach.concurrent.ScheduledExecutionContext

/**
 * A trait for an immutable builder of OptThrottleConfig. Callers may set the
 * optional throttle period by calling the throttle_ns method. If the throttle
 * method is never called then the optional throttle period is left unset.
 * @tparam MDT most derived type
 */
trait OptThrottleConfigBuilder[MDT <: OptThrottleConfigBuilder[MDT]] {
  /**
   * Set the optional throttle period
   * @param _throttle_ns the throttle period in nanoseconds
   * @return
   */
  def throttle_ns(
    _throttle_ns: Long
  )(implicit scheduledExecutionContext: ScheduledExecutionContext) :  MDT

  /**
   * Set the optional throttle period
   * @param _throttle the throttle period
   * @return
   */
  def throttle(
    _throttle: Duration
  )(implicit scheduledExecutionContext: ScheduledExecutionContext) :  MDT =
    throttle_ns(_throttle.toNanos)

  /** @return a ThrottleConfig with the optional throttle setting */
  def build() : OptThrottleConfig
}
