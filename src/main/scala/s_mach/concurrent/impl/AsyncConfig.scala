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
import SeriallyOps._

/**
 * A trait for the configuration of a concurrent serial TraversableOnce.async workflow that can wrap a concurrent
 * function with progress reporting, retry and throttling functions
 *
 * Note: Inheritance order here matters - throttle should be inner wrapper on f (progress and retry are interchangeable)
 */
trait AsyncConfig extends OptProgressConfig with OptRetryConfig with OptThrottleConfig

object AsyncConfig {
  case class AsyncConfigImpl(
    optProgress: Option[ProgressConfig] = None,
    optRetry: Option[RetryConfig] = None,
    optThrottle: Option[ThrottleConfig] = None
  ) extends AsyncConfig {
    override def optTotal = None
  }
  def apply(
    optProgress: Option[ProgressConfig] = None,
    optRetry: Option[RetryConfig] = None,
    optThrottle: Option[ThrottleConfig] = None
  )(implicit executionContext: ExecutionContext) : AsyncConfig = AsyncConfigImpl(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )
  def apply(cfg: AsyncConfig) : AsyncConfig = {
    import cfg._

    AsyncConfigImpl(
      optProgress = optProgress,
      optRetry = optRetry,
      optThrottle = optThrottle
    )
  }
}

trait AbstractAsyncConfigBuilder[MDT <: AbstractAsyncConfigBuilder[MDT]] extends
  ProgressConfigBuilder[MDT] with
  RetryConfigBuilder[MDT] with
  ThrottleConfigBuilder[MDT] with
  AsyncConfig with
  DelegatingTaskRunner
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
  def using(cfg: AsyncConfig) : MDT = {
    using(
      optProgress = cfg.optProgress,
      optRetry = cfg.optRetry,
      optThrottle = cfg.optThrottle
    )
  }

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
  override def retryer(r: Retryer)(implicit ec:ExecutionContext) =
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

trait AbstractAsyncConfigTaskRunner extends AsyncConfig with TaskRunner {
  lazy val taskHooks : Seq[TaskHook] = Seq(
    optProgress.map(ProgressState.apply)
  ).flatten

  lazy val taskStepHooks : Seq[TaskStepHook] = Seq(
    optThrottle.map(ThrottleState.apply),
    optRetry.map(RetryState.apply),
    optProgress.map(ProgressState.apply)
  ).flatten
}

/**
 * A builder for a configuration of a concurrent serial TraversableOnce workflow
 * @param ma the collection
 * @param optProgress optional progress report function
 * @param optRetry optional retry function
 * @param optThrottle optional throttle settings
 * @tparam A type of collection
 * @tparam M collection type
 */
case class AsyncConfigBuilder[A,M[+AA] <: TraversableOnce[AA]](
  ma: M[A],
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends
  AbstractAsyncConfigBuilder[AsyncConfigBuilder[A,M]] with
  AbstractAsyncConfigTaskRunner {

  override def optTotal = if(ma.hasDefiniteSize) {
    Some(ma.size)
  } else {
    None
  }

  def using(
    optProgress: Option[ProgressConfig] = optProgress,
    optRetry: Option[RetryConfig] = optRetry,
    optThrottle: Option[ThrottleConfig] = optThrottle
  ) = copy(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /** @return a copy of this config for a parallel workflow */
  def par = AsyncParConfigBuilder(
    ma = ma,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /** @return a copy of this config for a parallel workflow */
  def par(workerCount: Int) = AsyncParConfigBuilder(
    ma = ma,
    workerCount = workerCount,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  @inline def map[B](f: A => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    runTask1(ma, mapSerially[A,B,M], f)
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    runTask1(ma, flatMapSerially[A,B,M], f)
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    runTask1(ma, foreachSerially[A,U,M], f)
  }

  @inline def foldLeft[B](z:B)(f: (B,A) => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[B] = {
    val fSwap = { (a:A,b:B) => f(b,a) }
    runTask2[A,B,B,M,B](ma, foldLeftSerially[A,B,M](z), fSwap)
  }
}

