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
package s_mach


import java.util.concurrent.ScheduledExecutorService
import scala.language.higherKinds
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import scala.collection.generic.CanBuildFrom
import s_mach.concurrent.impl._

package object concurrent extends TupleConcurrentlyOps {
  implicit class SMach_Concurrent_PimpEverything[A](val self: A) extends AnyVal {
    @inline def future = Future.successful(self)
  }
  // Note: can't put value class in trait so this code has to be repeated in object Implicits and in package future
  implicit class SMach_Concurrent_PimpMyFutureType(val self:Future.type) extends AnyVal {
    @inline def delayed[A](delay: FiniteDuration)(f: () => A)(implicit
      scheduledExecutionContext:ScheduledExecutionContext
    ) : DelayedFuture[A] = scheduledExecutionContext.schedule(delay)(f)
    @inline def unit : Future[Unit] = FutureOps.unit
  }
  implicit class SMach_Concurrent_PimpMyFuture[A](val self: Future[A]) extends AnyVal {
    @inline def get: A = FutureOps.get(self)
    @inline def get(max: Duration): A = FutureOps.get(self,max)
    @inline def getTry: Try[A] = FutureOps.getTry(self)
    @inline def getTry(max: Duration): Try[A] = FutureOps.getTry(self, max)
    @inline def background(implicit ec: ExecutionContext) : Unit = FutureOps.background(self)
    @inline def toTry(implicit ec: ExecutionContext): Future[Try[A]] = FutureOps.toTry(self)
    @inline def fold[X](
      onSuccess: A => X,
      onFailure: Throwable => X
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.fold(self, onSuccess, onFailure)
    @inline def flatFold[X](
      onSuccess: A => Future[X],
      onFailure: Throwable => Future[X]
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.flatFold(self, onSuccess, onFailure)
    @inline def happensBefore[B](other: => Future[B])(implicit ec: ExecutionContext) : Future[B]
      = FutureOps.happensBefore(self, other)
    @inline def sideEffect(sideEffect: => Unit)(implicit ec: ExecutionContext) : Future[A]
      = FutureOps.sideEffect(self, sideEffect)
  }
  implicit class SMach_Concurrent_PimpMyFutureFuture[A](val self: Future[Future[A]]) extends AnyVal {
    @inline def flatten(implicit ec:ExecutionContext) : Future[A] = self.flatMap(v => v)
  }
  implicit class SMach_Concurrent_PimpMyTraversableFuture[A,M[AA] <: Traversable[AA]](
    val self: M[Future[A]]
  ) extends AnyVal {
    @inline def merge(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.merge(self)

    @inline def merge(atMost: Duration)(implicit
      ec: ExecutionContext,
      ses: ScheduledExecutorService,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.mergeTimeout(atMost, self)

    @inline def firstSuccess(implicit
      ec: ExecutionContext
    ) : Future[A] = FutureOps.firstSuccess(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableFutureTraversable[
    A,
    M[AA] <: Traversable[AA],
    N[AA] <: TraversableOnce[AA]
  ](
    val self: M[Future[N[A]]]
  ) extends AnyVal {
    @inline def flatMerge(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.flatMerge(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableOnceFuture[A,M[AA] <: TraversableOnce[AA]](
    val self: M[Future[A]]
  ) extends AnyVal {
    @inline def mergeAllFailures(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[M[Future[A]], Throwable, M[Throwable]]
    ) : Future[M[Throwable]] = MergeOps.mergeAllFailures(self)

    @inline def sequence(implicit
      cbf: CanBuildFrom[M[Future[A]], A, M[A]],
      ec: ExecutionContext
    ) : Future[M[A]] = Future.sequence(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableOnce[A,M[AA] <: TraversableOnce[AA]](val self: M[A]) extends AnyVal {
    @inline def serially(implicit ec:ExecutionContext) = new SeriallyConfigBuilder(self)

    @inline def workers(implicit ec:ExecutionContext) = WorkersConfigBuilder(self)
    @inline def workers(workerCount: Int)(implicit ec:ExecutionContext) = WorkersConfigBuilder(self, workerCount)
  }

  implicit class SMach_Concurrent_PimpMyTraversable[A,M[AA] <: Traversable[AA]](val self: M[A]) extends AnyVal {
    @inline def concurrently(implicit ec:ExecutionContext) = new ConcurrentlyConfigBuilder(self)
  }

}

