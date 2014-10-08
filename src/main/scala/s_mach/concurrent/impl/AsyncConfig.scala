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

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.util._
import s_mach.concurrent.ScheduledExecutionContext

/**
 * A trait for the configuration of a concurrent serial TraversableOnce.async workflow that can wrap a concurrent
 * function with progress reporting, retry and throttling functions
 *
 * Note: Inheritance order here matters - throttle should be inner wrapper on f (progress and retry are interchangeable)
 */
trait AsyncConfig extends OptProgressConfig with OptRetryConfig with OptThrottleConfig {
  def workerCount : Int
}

object AsyncConfig {
  val DEFAULT_PAR_WORKER_COUNT = Runtime.getRuntime.availableProcessors() * 2

  case class AsyncConfigImpl(
    workerCount: Int = 1,
    optProgress: Option[ProgressConfig] = None,
    optRetry: Option[RetryConfig] = None,
    optThrottle: Option[ThrottleConfig] = None
  ) extends AsyncConfig {
    override def optTotal = None
  }
  def apply(
    workerCount: Int = 1,
    optProgress: Option[ProgressConfig] = None,
    optRetry: Option[RetryConfig] = None,
    optThrottle: Option[ThrottleConfig] = None
  ) : AsyncConfig = AsyncConfigImpl(
    workerCount,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )
  def apply(cfg: AsyncConfig) : AsyncConfig = {
    import cfg._

    AsyncConfigImpl(
      workerCount = workerCount,
      optProgress = optProgress,
      optRetry = optRetry,
      optThrottle = optThrottle
    )
  }
}