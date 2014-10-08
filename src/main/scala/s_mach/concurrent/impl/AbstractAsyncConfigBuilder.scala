package s_mach.concurrent.impl

import scala.language.higherKinds
import s_mach.concurrent.ScheduledExecutionContext
import s_mach.concurrent.util.TaskEventListener
import scala.concurrent.ExecutionContext

trait AbstractAsyncConfigBuilder[MDT <: AbstractAsyncConfigBuilder[MDT]] extends
  ProgressConfigBuilder[MDT] with
  RetryConfigBuilder[MDT] with
  ThrottleConfigBuilder[MDT] with
  AsyncConfig
{

  def using(
    optProgress: Option[ProgressConfig] = optProgress,
    optRetry: Option[RetryConfig] = optRetry,
    optThrottle: Option[ThrottleConfig] = optThrottle
  ) : MDT

  /**
   * Copy an existing configuration
   * @param cfg configuration to use
   * @return a copy of the builder with all settings copied from cfg */
  def using(cfg: AsyncConfig) : MDT =
    using(
      optProgress = cfg.optProgress,
      optRetry = cfg.optRetry,
      optThrottle = cfg.optThrottle
    )


  /**
   * Set the optional progress reporting function
   * @return a copy of the builder with the new setting
   * */
  override def progress(r: TaskEventListener)(implicit ec:ExecutionContext) =
    using(
      optProgress = Some(ProgressConfig(
        optTotal = optTotal,
        reporter = r
      ))
    )

  /**
   * Set the optional retry function
   * @return a copy of the builder with the new setting
   * */
  override def retryDecider(r: RetryDecider)(implicit ec:ExecutionContext) =
    using(
      optRetry = Some(RetryConfig(r))
    )

  /**
   * Set the optional throttle setting in nanoseconds
   * @return a copy of the builder with the new setting
   * */
  override def throttle_ns(_throttle_ns: Long)(implicit sec:ScheduledExecutionContext) =
    using(
      optThrottle = Some(ThrottleConfig(_throttle_ns))
    )

  /** @return a config instance with the current settings */
  override def build() = AsyncConfig(this)
}

