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

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent._

/**
 * A trait for a builder of RetryConfig. Callers may set the optional retry function by calling the retry method. If the
 * retry function is never called then the optional retry function is left unset.
 * @tparam MDT most derived type
 */
trait RetryConfigBuilder[MDT <: RetryConfigBuilder[MDT]] {

  /**
   * Set the optional retry function
   * @param f a function that accepts a list of failures so far for an operation. The function returns TRUE if the
   *          operation should be retried.
   * @return a copy of the builder with the new setting
   */
  def retry(f: List[Throwable] => Future[Boolean]) : MDT

  /** @return a RetryConfig with the optional retry function */
  def build() : RetryConfig
}

/**
 * A trait for a function builder that can wrap a function to retry failures
 */
trait RetryConfig extends ConcurrentFunctionBuilder {
  implicit def executionContext: ExecutionContext

  /** @return the optional retry function */
  def optRetry : Option[List[Throwable] => Future[Boolean]]

  private[this] def doRetry[B](retry: List[Throwable] => Future[Boolean], f: => Future[B]) : Future[B] = {
    def loop(failures: List[Throwable]): Future[B] = {
      f.flatFold(
        onSuccess = { b: B => Future.successful(b)},
        onFailure = { t: Throwable =>
          val updatedFailures = t :: failures
          retry(updatedFailures) flatMap { shouldRetry =>
            if (shouldRetry) {
              loop(updatedFailures)
            } else {
              Future.failed(t)
            }
          }
        }
      )
    }
    loop(Nil)
  }

    /** @return if the optional retry function is set, a new function that retries f after failures. Otherwise, the
      *         function unmodified */
    override def build2[A,B](f: A => Future[B]) : A => Future[B] = {
    super.build2 {
      optRetry match {
        case Some(retry) => { a:A => doRetry(retry, f(a))}
        case None => f
      }
    }
  }
  
    /** @return if the optional retry function is set, a new function that retries f after failures. Otherwise, the
      *         function unmodified */
  override def build3[A,B,C](f: (A,B) => Future[C]) : (A,B) => Future[C] = {
    super.build3 {
      optRetry match {
        case Some(retry) => { (a:A,b:B) => doRetry(retry, f(a,b))}
        case None => f
      }
    }
  }

}