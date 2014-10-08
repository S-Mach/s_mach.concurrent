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

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent._
import s_mach.concurrent.util.Throttler

/**
 * A trait for a builder of ThrottleConfig. Callers may set the optional throttle period by calling the throttle_ns
 * method. If the throttle method is never called then the optional throttle period is left unset.
 * @tparam MDT most derived type
 */
trait ThrottleConfigBuilder[MDT <: ThrottleConfigBuilder[MDT]] {
  /**
   * Set the optional throttle period
   * @param _throttle_ns the throttle period in nanoseconds
   * @return
   */
  def throttle_ns(_throttle_ns: Long)(implicit scheduledExecutionContext: ScheduledExecutionContext) :  MDT

  /**
   * Set the optional throttle period
   * @param _throttle the throttle period
   * @return
   */
  def throttle(_throttle: Duration)(implicit scheduledExecutionContext: ScheduledExecutionContext) :  MDT =
    throttle_ns(_throttle.toNanos)

  /** @return a ThrottleConfig with the optional throttle setting */
  def build() : OptThrottleConfig
}

trait OptThrottleConfig {
  def optThrottle: Option[ThrottleConfig]
}

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


case class ThrottleState(
  throttle_ns: Long
)(implicit
  scheduledExecutionContext: ScheduledExecutionContext
) extends TaskStepHook {

  val throttler = Throttler(throttle_ns)

  override def hookStep0[R](f: StepId => Future[R])(implicit ec:ExecutionContext) : StepId => Future[R] = {
    { stepId:StepId => throttler.run(f(stepId)) }
  }

  override def hookStep1[A,R](f: (StepId,A) => Future[R])(implicit ec:ExecutionContext) : (StepId,A) => Future[R] = {
    { (stepId:StepId,a:A) => throttler.run(f(stepId,a)) }
  }

  override def hookStep2[A,B,R](f: (StepId,A,B) => Future[R])(implicit ec:ExecutionContext) : (StepId,A,B) => Future[R] = {
    { (stepId:StepId,a:A,b:B) => throttler.run(f(stepId,a,b)) }
  }
}

object ThrottleState {
  def apply(cfg: ThrottleConfig) : ThrottleState = ThrottleState(cfg.throttle_ns)(cfg.scheduledExecutionContext)
}