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
package s_mach.concurrent.config

import s_mach.concurrent._

/**
 * A trait for configuring optional throttling
 */
trait OptThrottleConfig {
  def optThrottle: Option[ThrottleConfig]
}

/**
 * A trait for configuring throttling
 */
trait ThrottleConfig {
  def scheduledExecutionContext: ScheduledExecutionContext
  def throttle_ns: Long
}

object ThrottleConfig {
  case class ThrottleConfigImpl(
    throttle_ns: Long
  )(implicit
    val scheduledExecutionContext: ScheduledExecutionContext
  ) extends ThrottleConfig

  def apply(
    throttle_ns: Long
  )(implicit
    scheduledExecutionContext: ScheduledExecutionContext
  ) : ThrottleConfig = ThrottleConfigImpl(throttle_ns)
}


