package s_mach.concurrent.impl

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

trait AbstractTraverseableOnceAsyncConfigBuilder[
  A,
  M[+AA] <: TraversableOnce[AA],
  MDT <: AbstractTraverseableOnceAsyncConfigBuilder[A,M,MDT]
] extends AbstractAsyncConfigBuilder[MDT] {
  def enumerator: M[A]

  def optTotal = if(enumerator.hasDefiniteSize) {
    Some(enumerator.size)
  } else {
    None
  }
}

case class TraverseableOnceAsyncConfigBuilder[A,M[+AA] <: TraversableOnce[AA]](
  enumerator: M[A],
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends AbstractTraverseableOnceAsyncConfigBuilder[
    A,
    M,
    TraverseableOnceAsyncConfigBuilder[A,M]
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
  def par = ParTraverseableOnceAsyncConfigBuilder[A,M](
    enumerator = enumerator,
    workerCount = AsyncConfig.DEFAULT_PAR_WORKER_COUNT,
    optProgress = optProgress,
    optRetry = optRetry,
    optThrottle = optThrottle
  )

  /** @return a copy of this config for a parallel workflow */
  def par(workerCount: Int) = ParTraverseableOnceAsyncConfigBuilder[A,M](
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
    AsyncTaskRunner(this).runTask2[A,B,B,M,B](enumerator, foldLeftSerially[A,B,M](z), fSwap)
  }
}

case class ParTraverseableOnceAsyncConfigBuilder[A,M[+AA] <: TraversableOnce[AA]](
  enumerator: M[A],
  workerCount: Int = AsyncConfig.DEFAULT_PAR_WORKER_COUNT,
  optProgress: Option[ProgressConfig] = None,
  optRetry: Option[RetryConfig] = None,
  optThrottle: Option[ThrottleConfig] = None
) extends AbstractTraverseableOnceAsyncConfigBuilder[
    A,
    M,
    ParTraverseableOnceAsyncConfigBuilder[A,M]
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
    AsyncTaskRunner(this).runTask1(enumerator, mapWorkers[A,B,M](workerCount), f)
  }

  @inline def flatMap[B](f: A => Future[TraversableOnce[B]])(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    AsyncTaskRunner(this).runTask1(enumerator, flatMapWorkers[A,B,M](workerCount), f)
  }

  @inline def foreach[U](f: A => Future[U])(implicit
    ec: ExecutionContext
  ) : Future[Unit] = {
    AsyncTaskRunner(this).runTask1(enumerator, foreachWorkers[A,U,M](workerCount), f)
  }
}