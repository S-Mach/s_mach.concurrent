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
package s_mach.concurrent.impl

import s_mach.concurrent.config.ThrottleConfig

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util.Throttler

case class ThrottleState(
  throttle_ns: Long
)(implicit
  scheduledExecutionContext: ScheduledExecutionContext
) extends TaskStepHook {

  val throttler = Throttler(throttle_ns)

  override def hookStepFunction0[R](
    f: TaskStepId => Future[R]
  )(implicit ec:ExecutionContext) : TaskStepId => Future[R] = {
    { stepId:TaskStepId => throttler.run(f(stepId)) }
  }

  override def hookStepFunction1[A,R](
    f: (TaskStepId,A) => Future[R]
  )(implicit ec:ExecutionContext) : (TaskStepId,A) => Future[R] = {
    { (stepId:TaskStepId,a:A) => throttler.run(f(stepId,a)) }
  }

  override def hookStepFunction2[A,B,R](
    f: (TaskStepId,A,B) => Future[R]
  )(implicit ec:ExecutionContext) : (TaskStepId,A,B) => Future[R] = {
    { (stepId:TaskStepId,a:A,b:B) => throttler.run(f(stepId,a,b)) }
  }
}

object ThrottleState {
  def apply(cfg: ThrottleConfig) : ThrottleState =
    ThrottleState(cfg.throttle_ns)(cfg.scheduledExecutionContext)
}