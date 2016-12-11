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
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach


import scala.language.higherKinds
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import scala.collection.generic.CanBuildFrom
import s_mach.concurrent.impl._
import s_mach.concurrent.config.{AsyncConfigBuilder, AsyncConfig}

/**
 * s_mach.concurrent is an open-source Scala library that provides asynchronous
 * serial and parallel execution flow control primitives for working with
 * asynchronous tasks. An asynchronous task consists of two or more calls to
 * function(s) that return a future result *<code>A ⇒ Future[B]</code> instead
 * of the result <code>A ⇒ B</code>.</p>
 *
 * Note: only difference between 2.11 and 2.12 version is Future.unit is removed
 * since its now part of std lib
 */
package object concurrent {

  implicit class SMach_Concurrent_PimpEverything[A](
    val self: A
  ) extends AnyVal {
    /** @return a successful Future of self */
    def future = Future.successful(self)
  }

  // Note: can't put value class in trait so this code has to be repeated in
  // object Implicits and in package future
  implicit class SMach_Concurrent_PimpMyFutureType(
    val self:Future.type
  ) extends AnyVal {
    /** @return a DelayedFuture that executes f after the specified delay */
    def delayed[A](delay: FiniteDuration)(f: => A)(implicit
      scheduledExecutionContext:ScheduledExecutionContext
    ) : DelayedFuture[A] = scheduledExecutionContext.schedule(delay)(f)
  }

  implicit class SMach_Concurrent_PimpMyFuture[A](
    val self: Future[A]
  ) extends AnyVal {
    /**
     * @return the result of the Future after it completes (Note: this waits
     * indefinitely for the Future to complete)
     * @throws java.lang.Exception Future completed with a failure, throws the exception
     * */
    def get: A = FutureOps.get(self)
    /**
     * @return the result of the Future after it completes
     * @throws java.util.concurrent.TimeoutException if Future does not complete within max duration
     * */
    def get(max: Duration): A = FutureOps.get(self,max)
    /**
     * @return the Try result of the Future after it completes (Note: this waits
     * indefinitely for the Future to complete)
     * */
    def getTry: Try[A] = FutureOps.getTry(self)
    /**
     * @return the Try result of the Future after it completes
     * @throws java.util.concurrent.TimeoutException if Future does not complete
     *         within max duration
     * */
    def getTry(max: Duration): Try[A] = FutureOps.getTry(self, max)
    /** Run future in the background. Discard the result of this Future but
      * ensure if there is an exception it gets reported to the ExecutionContext
      * */
    def background(implicit ec: ExecutionContext) : Unit =
      FutureOps.background(self)
    /** @return a Future of a Try of the result that always completes
      * successfully even if the Future eventually throws an exception
      * */
    def toTry(implicit ec: ExecutionContext): Future[Try[A]] =
      FutureOps.toTry(self)
    /** @return a Future of X that always succeeds. If self is successful, X
      * is derived from onSuccess otherwise if self is a failure, X is derived
      * from onFailure.
      * */
    def fold[X](
      onSuccess: A => X,
      onFailure: Throwable => X
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.fold(self, onSuccess, onFailure)
    /** @return a Future of X that always succeeds. If self is successful, X
      * is derived from onSuccess otherwise if self is a failure, X is derived
      * from onFailure.
      * */
    def flatFold[X](
      onSuccess: A => Future[X],
      onFailure: Throwable => Future[X]
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.flatFold(self, onSuccess, onFailure)
    /** @return a future of A that is guaranteed to happen before lhs */
    def happensBefore[B](
      other: => Future[B]
    )(implicit ec: ExecutionContext) : DeferredFuture[B]
      = FutureOps.happensBefore(self, other)
    /** @return execute a side effect after a future completes (even if it fails)
      * */
    def sideEffect(
      sideEffect: => Unit
    )(implicit ec: ExecutionContext) : Future[A]
      = FutureOps.sideEffect(self, sideEffect)
    /** @return a future that completes with fallback if the specified timeout
      *         is exceeded, otherwise the completed result of the future */
    def onTimeout(
      timeout: FiniteDuration
    )(
      fallback: => Future[A]
    )(implicit
      ec:ExecutionContext,
      sec: ScheduledExecutionContext
    ) : Future[A]
      = FutureOps.onTimeout(self, timeout)(fallback)
  }
  implicit class SMach_Concurrent_PimpMyFutureFuture[A](
    val self: Future[Future[A]]
  ) extends AnyVal {
    /** @return a future that completes once both the outer and inner future
      *         completes */
    def flatten(implicit ec:ExecutionContext) : Future[A] =
      self.flatMap(v => v)
  }
  implicit class SMach_Concurrent_PimpMyTraversableFuture[
    A,
    M[+AA] <: Traversable[AA]
  ](
    val self: M[Future[A]]
  ) extends AnyVal {
    /**
     * @return a Future of a collection of items that completes once all futures
     * are successful OR completes immediately after any failure. This is in
     * contrast to Future.sequence which will only complete once *all* Futures
     * have completed, even if one of the futures fails immediately. The first
     * failure encountered immediately throws AsyncParThrowable which has a
     * method to return a Future of all failures.
     **/
    def merge(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.merge(self)

    // TODO:
//    def merge(atMost: Duration)(implicit
//      ec: ExecutionContext,
//      ses: ScheduledExecutorService,
//      cbf: CanBuildFrom[Nothing, A, M[A]]
//    ) : Future[M[A]] = MergeOps.mergeTimeout(atMost, self)

    /**
     * @return the first successfully completed future. If all futures fail,
     * then completes the future with AsyncParThrowable of all failures.
     */
    def firstSuccess(implicit
      ec: ExecutionContext
    ) : Future[A] = FutureOps.firstSuccess(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableFutureTraversable[
    A,
    M[+AA] <: Traversable[AA],
    N[+AA] <: TraversableOnce[AA]
  ](
    val self: M[Future[N[A]]]
  ) extends AnyVal {
    /**
     * @return a Future of a collection of items that completes once all futures
     * are successful OR completes immediately after any failure. This is in
     * contrast to Future.sequence which will only complete once *all* Futures
     * have completed, even if one of the futures fails immediately. The first
     * failure encountered immediately throws AsyncParThrowable which has a
     * method to return a Future of all failures.
     **/
    def flatMerge(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.flatMerge(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableOnceFuture[
    A,
    M[AA] <: TraversableOnce[AA]
  ](
    val self: M[Future[A]]
  ) extends AnyVal {
    /** @return all failures thrown once all futures complete (maybe empty) */
    def mergeAllFailures(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[M[Future[A]], Throwable, M[Throwable]]
    ) : Future[M[Throwable]] = MergeOps.mergeAllFailures(self)
    /** @return a future of a collection of all results that completes
      *         successfully once all futures complete or that completes with
      *         the first failure encountered while scanning futures in left to
      *         right order */
    def sequence(implicit
      cbf: CanBuildFrom[M[Future[A]], A, M[A]],
      ec: ExecutionContext
    ) : Future[M[A]] = Future.sequence(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableOnce[
    A,
    M[+AA] <: TraversableOnce[AA]
  ](val self: M[A]) extends AnyVal {
    /** @return an asynchronous task runner for the collection */
    def async(implicit ec:ExecutionContext) =
      CollectionAsyncTaskRunner(self)
  }

  /** The global base asynchronous config builder configured with one worker
    * (serial) operation with all options disabled by default */
  val async = AsyncConfigBuilder()

  // Note: heterogeneous (tuple-based) asynchronous task processing is injected here
  implicit class SMach_Concurrent_PimpMyAsyncConfigBuilder(
    val self:AsyncConfig
  ) extends AnyVal with SMach_Concurrent_AbstractPimpMyAsyncConfig

}

