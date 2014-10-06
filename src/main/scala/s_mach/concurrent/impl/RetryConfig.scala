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

import s_mach.concurrent.util.{TaskStepHook, TaskHook}

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent._

case class SimpleRetryDecider(f: List[Throwable] => Future[Boolean])(implicit ec:ExecutionContext) extends RetryDecider {
  val failures = new java.util.concurrent.ConcurrentHashMap[Long, List[Throwable]]
  override def shouldRetry(stepId: Long, failure: Throwable): Future[Boolean] = {
    // Note: this test/execute is not atomic but this is ok since access to a particular step will always be synchronous
    val newFailList = failure :: Option(failures.get(stepId)).getOrElse(Nil)
    failures.put(stepId, newFailList)
    f(newFailList)
  }
}

/**
 * A trait for a builder of RetryConfig. Callers may set the optional retry function by calling the retry method. If the
 * retry function is never called then the optional retry function is left unset.
 * @tparam MDT most derived type
 */
trait RetryConfigBuilder[MDT <: RetryConfigBuilder[MDT]] {

  def retryDecider(r: RetryDecider)(implicit ec:ExecutionContext) : MDT

  /**
   * Set the optional retry function
   * @param f a function that accepts a list of failures so far for an operation. The function returns TRUE if the
   *          operation should be retried.
   * @return a copy of the builder with the new setting
   */
  def retry(f: List[Throwable] => Future[Boolean])(implicit ec:ExecutionContext) : MDT = {
    retryDecider(SimpleRetryDecider(f))
  }

  /** @return a RetryConfig with the optional retry function */
  def build() : OptRetryConfig
}

/**
 * A trait for a function builder that can wrap a function to retry failures
 */
trait OptRetryConfig {
  def optRetry: Option[RetryConfig]
}
trait RetryConfig {
  implicit def executionContext: ExecutionContext

  def retryer: RetryDecider
}

object RetryConfig {
  case class RetryConfigImpl(
    retryer: RetryDecider
  )(implicit
    val executionContext: ExecutionContext
  ) extends RetryConfig

  def apply(
    retryer: RetryDecider
  )(implicit
    executionContext: ExecutionContext
  ) : RetryConfig = RetryConfigImpl(retryer)
}


trait RetryDecider {
  def shouldRetry(stepId: Long, failure: Throwable) : Future[Boolean]
}

case class RetryState(retryer: RetryDecider) extends TaskStepHook {
  import TaskHook._

  override def hookStep0[R](step: StepId => Future[R])(implicit ec:ExecutionContext): StepId => Future[R] = {
    def loop(stepId: StepId): Future[R] = {
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

  override def hookStep1[A,R](step: (StepId,A) => Future[R])(implicit ec:ExecutionContext): (StepId,A) => Future[R] = {
    def loop(stepId: StepId, a: A): Future[R] = {
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

  override def hookStep2[A,B,R](step: (StepId,A,B) => Future[R])(implicit ec:ExecutionContext): (StepId,A,B) => Future[R] = {
    def loop(stepId: StepId, a: A, b: B): Future[R] = {
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