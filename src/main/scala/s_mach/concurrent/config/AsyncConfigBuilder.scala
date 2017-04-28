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
 * A case class for an immutable AsyncConfig builder
 * @param workerCount number of parallel workers to use during task
 * @param optProgress optional progress reporting settings
 * @param optRetry optional failure retry settings
 * @param optThrottle optional throttle setting
 */
case class AsyncConfigBuilder(
  workerCount: Int = 1,
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends AbstractAsyncConfigBuilder[AsyncConfigBuilder] {
  override def using(
    optProgress: Option[ProgressConfig],
    optRetry: Option[RetryConfig],
    optThrottle: Option[ThrottleConfig]
  )  = copy(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )
  override val optTotal = None

  def par = AsyncConfigBuilder(
    workerCount = AsyncConfig.DEFAULT_PAR_WORKER_COUNT,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  def par(workerCount: Int) = AsyncConfigBuilder(
    workerCount = workerCount,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )
}