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

import scala.concurrent.ExecutionContext
import s_mach.concurrent.util.RetryDecider

/**
 * A trait for configuring optional failure retry
 */
trait OptRetryConfig {
  def optRetry: Option[RetryConfig]
}

/**
 * A trait that configures failure retry
 */
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