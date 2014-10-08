package s_mach.concurrent.impl

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