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

import java.util.concurrent.ScheduledExecutorService
import s_mach.concurrent.ScheduledExecutionContext

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.util._
import SeriallyOps._

/**
 * A trait for the configuration of a TraversableOnce.serially workflow that can wrap a concurrent function with
 * progress reporting, retry and throttling functions
 *
 * Note: Inheritance order here matters - throttle should be inner wrapper on f (progress and retry are interchangable)
 */
trait SeriallyConfig extends ThrottleConfig with ProgressConfig with RetryConfig

object SeriallyConfig {
  case class SeriallyConfigImpl(
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
    optThrottle: Option[(Long, ScheduledExecutionContext)] = None
  )(implicit
    val executionContext:ExecutionContext
  ) extends SeriallyConfig
  def apply(
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
    optThrottle: Option[(Long, ScheduledExecutionContext)] = None
  )(implicit executionContext: ExecutionContext) : SeriallyConfig = SeriallyConfigImpl(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )
  def apply(cfg: SeriallyConfig) : SeriallyConfig = {
    import cfg._

    SeriallyConfigImpl(
      optProgress = optProgress,
      optRetry = optRetry,
      optThrottle = optThrottle
    )
  }
}

/**
 * A builder for a configuration of a TraversableOnce.serially workflow
 * @param ma the collection
 * @param optProgress optional progress report function
 * @param optRetry optional retry function
 * @param optThrottle optional throttle settings
 * @param executionContext execution context
 * @tparam A type of collection
 * @tparam M collection type
 */
case class SeriallyConfigBuilder[A,M[AA] <: TraversableOnce[AA]](
  ma: M[A],
  optProgress: Option[ProgressReporter] = None,
  optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
  optThrottle: Option[(Long, ScheduledExecutionContext)] = None
)(implicit
  val executionContext: ExecutionContext
) extends
  ProgressConfigBuilder[SeriallyConfigBuilder[A,M]] with
  RetryConfigBuilder[SeriallyConfigBuilder[A,M]] with
  ThrottleConfigBuilder[SeriallyConfigBuilder[A,M]] with
  SeriallyConfig {

  override protected def optTotal = if(ma.hasDefiniteSize) {
    Some(ma.size)
  } else {
    None
  }

  /**
   * Copy an existing configuration
   * @param cfg configuration to use 
   * @return a copy of the builder with all settings copied from cfg */
  def using(cfg: SeriallyConfig) = copy(
    optProgress = cfg.optProgress,
    optRetry = cfg.optRetry,
    optThrottle = cfg.optThrottle
  )

  /**
   * Set the optional progress reporting function
   * @return a copy of the builder with the new setting
   * */
  override def progress(r: ProgressReporter) = copy(optProgress = Some(r))

  /**
   * Set the optional retry function
   * @return a copy of the builder with the new setting
   * */
  override def retry(f: (List[Throwable]) => Future[Boolean]) = copy(optRetry = Some(f))

  /**
   * Set the optional throttle setting in nanoseconds
   * @return a copy of the builder with the new setting
   * */
  override def throttle_ns(_throttle_ns: Long)(implicit sec:ScheduledExecutionContext) = copy(optThrottle = Some((_throttle_ns, sec)))

  /** @return a SeriallyConfig with the current settings */
  override def build = SeriallyConfig(this)

  @inline def map[B](f: A => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = loop(mapSerially(ma, build2(f)))

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = loop(flatMapSerially(ma, build2(f)))

  @inline def foreach[U](f: A => Future[U]) : Future[Unit] = loop(foreachSerially(ma, build2(f)))

  @inline def foldLeft[B](z:B)(f: (B,A) => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[B] = loop(foldLeftSerially(ma, z, build3(f)))
}

