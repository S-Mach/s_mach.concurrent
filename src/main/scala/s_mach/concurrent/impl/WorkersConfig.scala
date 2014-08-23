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

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import s_mach.concurrent.util._
import WorkersOps._

/**
 * A trait for the configuration of a TraversableOnce.serially workflow that can wrap a concurrent function with
 * progress reporting, retry and throttling functions
 *
 * Note: Inheritance order here matters - throttle should be inner wrapper on f (progress and retry are interchangable)
 */
trait WorkersConfig extends ThrottleConfig with ProgressConfig with RetryConfig {
  def workerCount : Int
}

object WorkersConfig {
  val DEFAULT_WORKER_COUNT = Runtime.getRuntime.availableProcessors()

  case class WorkersConfigImpl(
    workerCount: Int = DEFAULT_WORKER_COUNT,
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
    optThrottle: Option[(Long, ScheduledExecutionContext)] = None
  )(implicit
    val executionContext:ExecutionContext
  ) extends WorkersConfig
  def apply(
    workerCount: Int = DEFAULT_WORKER_COUNT,
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
    optThrottle: Option[(Long, ScheduledExecutionContext)] = None
  )(implicit executionContext: ExecutionContext) : WorkersConfig = WorkersConfigImpl(
    optProgress = optProgress, 
    optRetry = optRetry, 
    optThrottle = optThrottle,
    workerCount = workerCount
  )
  def apply(cfg: WorkersConfig) : WorkersConfig = {
    import cfg._
    
    WorkersConfigImpl(
      optProgress = optProgress, 
      optRetry = optRetry, 
      optThrottle = optThrottle,
      workerCount = workerCount
    )
  }
}

/**
 * A builder for a configuration of a TraversableOnce.workers workflow
 * @param ma the collection
 * @param workerCount count of workers
 * @param optProgress optional progress report function
 * @param optRetry optional retry function
 * @param optThrottle optional throttle settings
 * @param executionContext execution context
 * @tparam A type of collection
 * @tparam M collection type
 */
case class WorkersConfigBuilder[A,M[AA] <: TraversableOnce[AA]](
  ma: M[A],
  workerCount: Int = WorkersConfig.DEFAULT_WORKER_COUNT,
  optProgress: Option[ProgressReporter] = None,
  optRetry: Option[(List[Throwable]) => Future[Boolean]] = None,
  optThrottle: Option[(Long, ScheduledExecutionContext)] = None
)(implicit
  val executionContext: ExecutionContext
) extends
  ProgressConfigBuilder[WorkersConfigBuilder[A,M]] with
  RetryConfigBuilder[WorkersConfigBuilder[A,M]] with
  ThrottleConfigBuilder[WorkersConfigBuilder[A,M]] with
  WorkersConfig {

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

  /** @return a WorkersConfig with the current settings */
  override def build = WorkersConfig(this)

  @inline def map[B](f: A => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = loop(mapWorkers(ma, build2(f), this))

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = loop(flatMapWorkers(ma, build2(f), this))

  @inline def foreach[U](f: A => Future[U]) : Future[Unit] = loop(foreachWorkers(ma, build2(f), this))
}

