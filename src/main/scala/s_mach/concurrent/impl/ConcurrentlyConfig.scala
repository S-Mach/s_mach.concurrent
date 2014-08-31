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

import scala.collection.generic.CanBuildFrom
import scala.concurrent._
import scala.language.higherKinds
import s_mach.concurrent.util._
import ConcurrentlyOps._

/**
 * A trait for the configuration of a Traversable.concurrently workflow that can wrap a concurrent function with 
 * progress reporting and retry functions
 */
trait ConcurrentlyConfig extends ProgressConfig with RetryConfig

object ConcurrentlyConfig {
  case class ConcurrentlyConfigImpl(
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None
  )(implicit
    val executionContext:ExecutionContext
  ) extends ConcurrentlyConfig
  def apply(
    optProgress: Option[ProgressReporter] = None,
    optRetry: Option[(List[Throwable]) => Future[Boolean]] = None
  )(implicit executionContext: ExecutionContext) : ConcurrentlyConfig = ConcurrentlyConfigImpl(
    optProgress = optProgress, 
    optRetry = optRetry
  )
  def apply(cfg: ConcurrentlyConfig) : ConcurrentlyConfig = {
    import cfg._
    
    ConcurrentlyConfigImpl(
      optProgress = optProgress, 
      optRetry = optRetry
    )
  }
}

/**
 * A builder for a configuration of a Traversable.concurrently workflow
 * @param ma the collection
 * @param optProgress optional progress report function
 * @param optRetry optional retry function
 * @param executionContext execution context
 * @tparam A type of collection
 * @tparam M collection type
 */
case class ConcurrentlyConfigBuilder[A,M[+AA] <: Traversable[AA]](
  ma: M[A],
  optProgress: Option[ProgressReporter] = None,
  optRetry: Option[(List[Throwable]) => Future[Boolean]] = None
)(implicit val executionContext:ExecutionContext) extends
  ProgressConfigBuilder[ConcurrentlyConfigBuilder[A,M]] with
  RetryConfigBuilder[ConcurrentlyConfigBuilder[A,M]] with
  ConcurrentlyConfig
{

  override protected def optTotal = if(ma.hasDefiniteSize) {
    Some(ma.size)
  } else {
    None
  }

  /**
   * Copy an existing configuration
   * @param cfg configuration to use
   * @return a copy of the builder with all settings copied from cfg */
  def using(cfg: ConcurrentlyConfig) = copy(
    optProgress = cfg.optProgress,
    optRetry = cfg.optRetry
  )

  /**
   * Set the optional progress reporting function
   * @return a copy of the builder with the new setting
   * */
  override def progress(reporter: ProgressReporter) = copy(optProgress = Some(reporter))
  
  /**
   * Set the optional retry function
   * @return a copy of the builder with the new setting
   * */
  override def retry(f: (List[Throwable]) => Future[Boolean]) = copy(optRetry = Some(f))

  /** @return a ConcurrentlyConfig with the current settings */
  def build = ConcurrentlyConfig(this)

  @inline def map[B](f: A => Future[B])(implicit
    cbf1: CanBuildFrom[Nothing, Future[B], M[Future[B]]],
    cbf2: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = runLoop(mapConcurrently(ma, build2(f)))

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf1: CanBuildFrom[Nothing, Future[TraversableOnce[B]], M[Future[TraversableOnce[B]]]],
    cbf2: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = runLoop(flatMapConcurrently(ma, build2(f)))

  @inline def foreach[U](f: A => Future[U])(implicit
    cbf1: CanBuildFrom[Nothing, Future[U], M[Future[U]]],
    cbf2: CanBuildFrom[Nothing, U, M[U]]
  ) : Future[Unit] = runLoop(foreachConcurrently(ma, build2(f)))
}

