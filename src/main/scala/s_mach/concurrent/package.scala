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
import s_mach.concurrent.config.{AsyncConfigBuilder, AsyncConfig}

package object concurrent {
  
  implicit class SMach_Concurrent_PimpEverything[A](
    val self: A
  ) extends AnyVal {
    def future = Future.successful(self)
  }
  
  // Note: can't put value class in trait so this code has to be repeated in
  // object Implicits and in package future
  implicit class SMach_Concurrent_PimpMyFutureType(
    val self:Future.type
  ) extends AnyVal {
    def delayed[A](delay: FiniteDuration)(f: => A)(implicit
      scheduledExecutionContext:ScheduledExecutionContext
    ) : DelayedFuture[A] = scheduledExecutionContext.schedule(delay)(f)
    def unit : Future[Unit] = FutureOps.unit
  }
  
  implicit class SMach_Concurrent_PimpMyFuture[A](
    val self: Future[A]
  ) extends AnyVal {
    def get: A = FutureOps.get(self)
    def get(max: Duration): A = FutureOps.get(self,max)
    def getTry: Try[A] = FutureOps.getTry(self)
    def getTry(max: Duration): Try[A] = FutureOps.getTry(self, max)
    def background(implicit ec: ExecutionContext) : Unit = 
      FutureOps.background(self)
    def toTry(implicit ec: ExecutionContext): Future[Try[A]] = 
      FutureOps.toTry(self)
    def fold[X](
      onSuccess: A => X,
      onFailure: Throwable => X
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.fold(self, onSuccess, onFailure)
    def flatFold[X](
      onSuccess: A => Future[X],
      onFailure: Throwable => Future[X]
    )(implicit
      ec:ExecutionContext
    ) : Future[X] = FutureOps.flatFold(self, onSuccess, onFailure)
    def happensBefore[B](
      other: => Future[B]
    )(implicit ec: ExecutionContext) : DeferredFuture[B]
      = FutureOps.happensBefore(self, other)
    def sideEffect(
      sideEffect: => Unit
    )(implicit ec: ExecutionContext) : Future[A]
      = FutureOps.sideEffect(self, sideEffect)
  }
  implicit class SMach_Concurrent_PimpMyFutureFuture[A](
    val self: Future[Future[A]]
  ) extends AnyVal {
    def flatten(implicit ec:ExecutionContext) : Future[A] = 
      self.flatMap(v => v)
  }
  implicit class SMach_Concurrent_PimpMyTraversableFuture[
    A, 
    M[+AA] <: Traversable[AA]
  ](
    val self: M[Future[A]]
  ) extends AnyVal {
    def merge(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[Nothing, A, M[A]]
    ) : Future[M[A]] = MergeOps.merge(self)

//    def merge(atMost: Duration)(implicit
//      ec: ExecutionContext,
//      ses: ScheduledExecutorService,
//      cbf: CanBuildFrom[Nothing, A, M[A]]
//    ) : Future[M[A]] = MergeOps.mergeTimeout(atMost, self)

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
    def mergeAllFailures(implicit
      ec: ExecutionContext,
      cbf: CanBuildFrom[M[Future[A]], Throwable, M[Throwable]]
    ) : Future[M[Throwable]] = MergeOps.mergeAllFailures(self)

    def sequence(implicit
      cbf: CanBuildFrom[M[Future[A]], A, M[A]],
      ec: ExecutionContext
    ) : Future[M[A]] = Future.sequence(self)
  }

  implicit class SMach_Concurrent_PimpMyTraversableOnce[
    A,
    M[+AA] <: TraversableOnce[AA]
  ](val self: M[A]) extends AnyVal {
    def async(implicit ec:ExecutionContext) =
      TraverseableOnceAsyncConfigBuilder(self)
  }

  val async = AsyncConfigBuilder()

  implicit class SMach_Concurrent_PimpMyAsyncConfigBuilder(
    val self:AsyncConfig
  ) extends AnyVal with SMach_Concurrent_AbstractPimpMyAsyncConfig
}

