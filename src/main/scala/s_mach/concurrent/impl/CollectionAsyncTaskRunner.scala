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
package s_mach.concurrent.impl

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.config._

trait AbstractCollectionAsyncTaskRunner[
  A,
  M[+AA] <: TraversableOnce[AA],
  MDT <: AbstractCollectionAsyncTaskRunner[A,M,MDT]
] extends AbstractAsyncConfigBuilder[MDT] {
  def input: M[A]

  def optTotal = if(input.hasDefiniteSize) {
    Some(input.size)
  } else {
    None
  }
}

/**
 * A case class for a serial asynchronous task runner that is configurable with
 * optional progress reporting, throttling and/or failure retry.
 * @param input the input collection
 * @param optProgress optional progress reporting settings
 * @param optRetry optional failure retry settings
 * @param optThrottle optional throttle settings
 * @tparam A the input type
 * @tparam M the collection type
 */
case class CollectionAsyncTaskRunner[A,M[+AA] <: TraversableOnce[AA]](
  input: M[A],
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

  /** @return a parallel async task runner configured with a copy of all
    *         settings  */
  def par = ParCollectionAsyncTaskRunner[A,M](
    input = input,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /**
   * @return a parallel async task runner configured to run with workerCount
   *         workers and with a copy of all settings  */
  def par(workerCount: Int) = ParCollectionAsyncTaskRunner[A,M](
    input = input,
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
    AsyncTaskRunner(this).runTask1(input, mapSerially[A,B,M], f)
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(input, flatMapSerially[A,B,M], f)
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    AsyncTaskRunner(this).runTask1(input, foreachSerially[A,U,M], f)
  }

  @inline def foldLeft[B](z:B)(f: (B,A) => Future[B])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[B] = {
    val fSwap = { (a:A,b:B) => f(b,a) }
    AsyncTaskRunner(this).runTask2[A,B,B,M,B](
      input,
      foldLeftSerially[A,B,M](z),
      fSwap
    )
  }
}

/**
 * A case class for a parallel asynchronous task runner that is configurable 
 * with optional progress reporting, throttling and/or failure retry.
 * @param input the input collection
 * @param optProgress optional progress reporting settings
 * @param optRetry optional failure retry settings
 * @param optThrottle optional throttle settings
 * @tparam A the input type
 * @tparam M the collection type
 */
case class ParCollectionAsyncTaskRunner[
  A,
  M[+AA] <: TraversableOnce[AA]
](
  input: M[A],
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
      input,
      mapWorkers[A,B,M](workerCount),
      f
    )
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(
      input,
      flatMapWorkers[A,B,M](workerCount),
      f
    )
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    AsyncTaskRunner(this).runTask1(
      input,
      foreachWorkers[A,U,M](workerCount),
      f
    )
  }
}