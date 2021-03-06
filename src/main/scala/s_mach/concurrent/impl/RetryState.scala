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
package s_mach.concurrent.impl

import s_mach.concurrent.config.RetryConfig
import s_mach.concurrent.util.RetryDecider

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent._

case class RetryState(retryer: RetryDecider) extends TaskStepHook {

  override def hookStepFunction0[R](
    step: TaskStepId => Future[R]
  )(implicit ec:ExecutionContext): TaskStepId => Future[R] = {
    def loop(stepId: TaskStepId): Future[R] = {
      step(stepId).flatFold(
        onSuccess = { r:R => Future.successful(r)},
        onFailure = { t: Throwable =>
          retryer.shouldRetry(stepId, t).flatMap { shouldRetry =>
            if(shouldRetry) {
              loop(stepId)
            } else {
              Future.failed(t)
            }
          }}
      )
    }

    loop
  }

  override def hookStepFunction1[A,R](
    step: (TaskStepId,A) => Future[R]
  )(implicit ec:ExecutionContext): (TaskStepId,A) => Future[R] = {
    def loop(stepId: TaskStepId, a: A): Future[R] = {
      step(stepId, a).flatFold(
        onSuccess = { r:R => Future.successful(r)},
        onFailure = { t: Throwable =>
          retryer.shouldRetry(stepId, t).flatMap { shouldRetry =>
            if(shouldRetry) {
              loop(stepId, a)
            } else {
              Future.failed(t)
            }
          }}
      )
    }

    loop
  }

  override def hookStepFunction2[A,B,R](
    step: (TaskStepId,A,B) => Future[R]
  )(implicit ec:ExecutionContext): (TaskStepId,A,B) => Future[R] = {
    def loop(stepId: TaskStepId, a: A, b: B): Future[R] = {
      step(stepId, a, b).flatFold(
        onSuccess = { r:R => Future.successful(r)},
        onFailure = { t: Throwable =>
          retryer.shouldRetry(stepId, t).flatMap { shouldRetry =>
            if(shouldRetry) {
              loop(stepId, a, b)
            } else {
              Future.failed(t)
            }
          }}
      )
    }

   loop
  }
}

object RetryState {
  def apply(cfg: RetryConfig) : RetryState = RetryState(cfg.retryer)
}