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

import java.util.concurrent.{ScheduledExecutorService, LinkedBlockingQueue, TimeUnit, ThreadPoolExecutor}
import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import s_mach.concurrent._

object MergeOps extends MergeOps {
  /**
   * An ExecutionContext for handling Futures that result from failures in a merge. This is required to prevent
   * the Futures already in the queue from completing before the Future handling Failure detection.
   * */
  private val failureExecutionContext = {
    // Note: this pool only ever has to execute p.tryComplete and return so should not be heavily in demand
    val failureExecutor = new ThreadPoolExecutor(
      /* int corePoolSize */ 1,
      /* int maximumPoolSize */ Math.min(1,Runtime.getRuntime.availableProcessors() / 2),
      /* long keepAliveTime */ 1,
      /* TimeUnit unit */ TimeUnit.SECONDS,
      /* BlockingQueue<Runnable> workQueue */ new LinkedBlockingQueue[Runnable]()
    )
    ExecutionContext.fromExecutor(failureExecutor)
  }
}

trait MergeOps {
  /** @return all failures thrown once all futures complete (maybe empty) */
  def mergeAllFailures[A, M[AA] <: TraversableOnce[AA]](
    zomFuture: M[Future[A]]
  )(implicit
    ec: ExecutionContext,
    cbf: CanBuildFrom[M[Future[A]], Throwable, M[Throwable]]
  ) : Future[M[Throwable]] = {
    zomFuture.foldLeft(Future.successful(cbf(zomFuture))) { (fr, fa) =>
      for {
        r <- fr
        a <- fa.toTry
      } yield {
        a match {
          case Success(_) => r
          case Failure(t) => r += t
        }
      }
    } map (_.result())
  }

  /** Wait on any failure from zomFuture. Immediately after the first failure, fail the promise with
    * ConcurrentThrowable. */
  def mergeFailImmediately[A](
    p: Promise[A],
    zomFuture: Traversable[Future[Any]]
  )(implicit
    ec:ExecutionContext
  ) : Unit = {
    val doFail : PartialFunction[Throwable, Unit] = { case t =>
      lazy val futAllFailure = {
        // Note: important to use the failure execution context here since this value is lazy and ec may have been
        // shutdown by the time it is evaluated
        implicit val ec = MergeOps.failureExecutionContext
        mergeAllFailures(zomFuture.toVector)
      }
      p.tryFailure(ConcurrentThrowable(t,futAllFailure))
    }
    // Note: using failureExecutionContext here to allow immediately failing the Promise. If ec was used then
    // p.tryFailure wouldn't be executed until after ALL zomFuture had completed since they entered ec's
    // queue first.
    zomFuture.foreach(_.onFailure(doFail)(MergeOps.failureExecutionContext))
  }

  /**
   * @return a Future of a collection of items that completes once all futures are successful OR completes immediately
   *         after any failure. This is in contrast to Future.sequence which will only complete once *all* Futures have
   *         completed, even if one of the futures fails immediately. The first failure encountered immediately throws
   *         ConcurrentThrowable which has a method to return a Future of all failures.
   **/
  def merge[A, M[AA] <: Traversable[AA]](
    zomFuture: M[Future[A]]
  )(implicit
    ec: ExecutionContext,
    cbf1: CanBuildFrom[Nothing, A, M[A]]
  ) : Future[M[A]] = {
    val promise = Promise[M[A]]()
    // concurrently wait on any future failure
    mergeFailImmediately(promise, zomFuture)
    // Note: not using p.tryCompleteWith to prevent race condition on failure of first Future - only path for failure is
    // mergeFailImmediately otherwise non-ConcurrentThrowable can leak
    Future.sequence(zomFuture)(scala.collection.breakOut(cbf1), ec) onSuccess { case v => promise.success(v) }
    promise.future
  }

  /**
   * @return a Future of a collection of items that completes once all futures are successful OR completes immediately
   *         after any failure. This is in contrast to Future.sequence which will only complete once *all* Futures have
   *         completed, even if one of the futures fails immediately. The first failure encountered immediately throws
   *         ConcurrentThrowable which has a method to return a Future of all failures.
   **/
  def flatMerge[A, M[AA] <: Traversable[AA], N[AA] <: TraversableOnce[AA]](
    zomFuture: M[Future[N[A]]]
  )(implicit
    ec: ExecutionContext,
    cbf1: CanBuildFrom[Nothing, A, M[A]]
  ) : Future[M[A]] = {
    val promise = Promise[M[A]]()

    MergeOps.mergeFailImmediately(promise, zomFuture)
    // Note: not using Future.sequence here to side step having to create a temporary M[Future[N[A]]]
    zomFuture.foldLeft(Future.successful(cbf1())) { (fbuilder, fZomA) =>
      for {
        builder <- fbuilder
        zomA <- fZomA
      } yield builder ++= zomA
    } onSuccess { case builder => promise.success(builder.result()) }

    promise.future
  }


  /**
   * @return a Future of a collection of items that completes once all futures are successful OR completes immediately
   *         after any failure OR completes after the specified timeout. If the timeout is reached all pending futures
   *         are discarded. Exceptions that occur after timeout are reported to ExecutionContext.
   **/
  def mergeTimeout[A, M[AA] <: Traversable[AA]](
    atMost: Duration,
    zomFuture: M[Future[A]]
  )(implicit
    ec: ExecutionContext,
    ses: ScheduledExecutorService,
    cbf1: CanBuildFrom[Nothing, A, M[A]]
  ) : Future[M[A]] = {
    // Avoid race conditions between scheduled runnable and merge below
    val lock = new Object
    val promise = Promise[M[A]]()
    ses.schedule(
      new Runnable {
        override def run() {
          lock.synchronized {
          if(promise.isCompleted == false) {
              // Results not completed now are discarded
              val (_completedNow,notCompletedNow) = zomFuture.partition(_.isCompleted)
              // Note: this means exceptions that occur after now will not be seen by caller
              // Ensure all exceptions are at least reported to execution context so they don't disappear forever
              notCompletedNow.foreach(_.background)
              val (_nowSuccess, _nowFailures) = _completedNow.map(_.value.get).partition(_.isSuccess)
              val nowSuccess = _nowSuccess.map(_.get)
              val nowFailures = _nowFailures.map(_.failed.get)
              lazy val _futAllFailure =
                Future.sequence(zomFuture.map(_.toTry))
                  .map(_.collect { case Failure(t) => t }.toVector)
              if(nowFailures.isEmpty) {
                val builder = cbf1()
                builder ++= nowSuccess
                promise.success(builder.result())
              } else {
                promise.failure(new ConcurrentThrowable {
                  override def firstFailure = nowFailures.head
                  override def allFailure = _futAllFailure
                })
              }
            }
          }
        }
      },
      atMost.toNanos,
      TimeUnit.NANOSECONDS
    )
    merge(zomFuture) onComplete {  case t => lock.synchronized { promise.tryComplete(t) } }
    promise.future
  }
}
