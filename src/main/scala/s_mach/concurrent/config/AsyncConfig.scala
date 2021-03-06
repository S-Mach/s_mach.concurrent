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

/**
 * A trait for an asynchronous task configuration that allows configuring
 * the number of parallel workers used to process the task, optional progress
 * reporting, failure retry and throttling.
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