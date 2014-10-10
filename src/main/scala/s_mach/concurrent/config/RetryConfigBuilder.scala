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
package s_mach.concurrent.config

import s_mach.concurrent.impl.SimpleRetryDecider
import s_mach.concurrent.util.RetryDecider

import scala.concurrent.{ExecutionContext, Future}

/**
 * A trait for a builder of RetryConfig. Callers may set the optional retry
 * function by calling the retry method. If the retry function is never called
 * then the optional retry function is left unset.
 * @tparam MDT most derived type
 */
trait RetryConfigBuilder[MDT <: RetryConfigBuilder[MDT]] {

  def retryDecider(r: RetryDecider)(implicit ec:ExecutionContext) : MDT

  /**
   * Set the optional retry function
   * @param f a function that accepts a list of failures so far for an
   *          operation. The function returns TRUE if the operation should be
   *          retried.
   * @return a copy of the builder with the new setting
   */
  def retry(
    f: List[Throwable] => Future[Boolean]
  )(implicit ec:ExecutionContext) : MDT = {
    retryDecider(SimpleRetryDecider(f))
  }

  /** @return a RetryConfig with the optional retry function */
  def build() : OptRetryConfig
}

