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

import s_mach.concurrent.config._
import s_mach.concurrent.util._

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

trait AbstractCollectionAsyncTaskRunner[
  A,
  M[+AA] <: TraversableOnce[AA],
  MDT <: AbstractCollectionAsyncTaskRunner[A,M,MDT]
] extends AbstractAsyncConfigBuilder[MDT] {
  def enumerator: M[A]

  def optTotal = if(enumerator.hasDefiniteSize) {
    Some(enumerator.size)
  } else {
    None
  }
}

case class CollectionAsyncTaskRunner[A,M[+AA] <: TraversableOnce[AA]](
  enumerator: M[A],
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends AbstractCollectionAsyncTaskRunner[
    A,
    M,
    CollectionAsyncTaskRunner[A,M]
  ] {

  val workerCount = 1

  def using(
    optProgress: Option[ProgressConfig] = optProgress,
    optRetry: Option[RetryConfig] = optRetry,
    optThrottle: Option[ThrottleConfig] = optThrottle
  )  = copy(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /** @return a copy of this config for a parallel workflow */
  def par = ParCollectionAsyncTaskRunner[A,M](
    enumerator = enumerator,
    workerCount = AsyncConfig.DEFAULT_PAR_WORKER_COUNT,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /** @return a copy of this config for a parallel workflow */
  def par(workerCount: Int) = ParCollectionAsyncTaskRunner[A,M](
    enumerator = enumerator,
    workerCount = workerCount,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  import SeriallyOps._
  @inline def map[B](f: A => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(enumerator, mapSerially[A,B,M], f)
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(enumerator, flatMapSerially[A,B,M], f)
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    AsyncTaskRunner(this).runTask1(enumerator, foreachSerially[A,U,M], f)
  }

  @inline def foldLeft[B](z:B)(f: (B,A) => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[B] = {
    val fSwap = { (a:A,b:B) => f(b,a) }
    AsyncTaskRunner(this).runTask2[A,B,B,M,B](
      enumerator,
      foldLeftSerially[A,B,M](z),
      fSwap
    )
  }
}

case class ParCollectionAsyncTaskRunner[
  A,
  M[+AA] <: TraversableOnce[AA]
](
  enumerator: M[A],
  workerCount: Int = AsyncConfig.DEFAULT_PAR_WORKER_COUNT,
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends AbstractCollectionAsyncTaskRunner[
    A,
    M,
    ParCollectionAsyncTaskRunner[A,M]
  ] {

  def using(
    optProgress: Option[ProgressConfig] = optProgress,
    optRetry: Option[RetryConfig] = optRetry,
    optThrottle: Option[ThrottleConfig] = optThrottle
  )  = copy(
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  import WorkersOps._
  @inline def map[B](f: A => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(
      enumerator,
      mapWorkers[A,B,M](workerCount),
      f
    )
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(
      enumerator,
      flatMapWorkers[A,B,M](workerCount),
      f
    )
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    AsyncTaskRunner(this).runTask1(
      enumerator,
      foreachWorkers[A,U,M](workerCount),
      f
    )
  }
}