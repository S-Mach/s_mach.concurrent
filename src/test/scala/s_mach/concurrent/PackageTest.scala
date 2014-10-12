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
package s_mach.concurrent

import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent._
import scala.concurrent.duration._
import s_mach.concurrent.util.{Latch, Barrier}
import s_mach.concurrent.impl._

import scala.util.{Failure, Success}

class PackageTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "package concurrent" must "provide convenience methods to various implementations" in {
    implicit val ctc = mkConcurrentTestContext()

    1.future shouldBe a[Future[Int]]
    1.future.get should equal(1)

    Future.unit shouldBe a [Future[Unit]]
    Future.unit.get should equal(())

    val latch = Latch()
    an [TimeoutException] should be thrownBy latch.future.get(1.nanos)
    an [TimeoutException] should be thrownBy latch.future.getTry(1.nanos)

    val ex = new RuntimeException
    Future.successful(1).getTry should equal(Success(1))
    Future.failed(ex).getTry should equal(Failure(ex))

    Future.successful(1).toTry.get should equal(Success(1))
    Future.failed(ex).toTry.get should equal(Failure(ex))

    Future.successful(1).fold(
      onSuccess = { i:Int => i.toString },
      onFailure = { t:Throwable => t.toString }
    ).get should equal("1")

    Future.failed(ex).fold(
      onSuccess = { i:Int => i.toString },
      onFailure = { t:Throwable => t.toString }
    ).get should equal("java.lang.RuntimeException")

    Future.successful(1).flatFold(
      onSuccess = { i:Int => i.toString.future },
      onFailure = { t:Throwable => t.toString.future }
    ).get should equal("1")

    Future.failed(ex).flatFold(
      onSuccess = { i:Int => i.toString.future },
      onFailure = { t:Throwable => t.toString.future }
    ).get should equal("java.lang.RuntimeException")

    Future.successful(Future.successful(1)).flatten.get should equal(1)
    Future.successful(Future.failed(ex)).flatten.getTry should equal(Failure(ex))
    Future.failed(ex).flatten.getTry should equal(Failure(ex))

    val items = Vector(1,2,3)
    items.async should equal(CollectionAsyncTaskRunner(items))
    items.async.par should equal(CollectionAsyncParTaskRunner(items))
    items.async.par(1) should equal(CollectionAsyncParTaskRunner(items,1))
  }

  "Future.sideEffect" must "execute the side effect after the future completes" in {
    implicit val ctc = mkConcurrentTestContext()
    val ex = new RuntimeException
    val sideEffects = List.newBuilder[String]

    Future.successful(1).sideEffect { sideEffects += "1" }
    Future.failed(ex).sideEffect { sideEffects += "1" }

    ctc.waitForActiveExecutionCount(0)
    sideEffects.result() should equal(List("1","1"))
  }

  "Future.background" must "ensure exceptions are reported to ExecutionContext" in {
    val ctc = mkConcurrentTestContext()
    val failures = List.newBuilder[Throwable]
    implicit val ec = new ExecutionContext {
      override def reportFailure(cause: Throwable) = failures += cause
      override def execute(runnable: Runnable): Unit = ctc.execute(runnable)
    }
    val ex = new RuntimeException
    Future { throw ex }.background
    ctc.waitForActiveExecutionCount(0)
    failures.result() should equal(List(ex))
  }

  "Future.delayed" must "execute a task after a specified delay" in {
    implicit val ctc = mkConcurrentTestContext()

    val startTime_ns = System.nanoTime() + 1.millis.toNanos
    val delayedFuture = Future.delayed(1.millis) {
      1
    }
    delayedFuture.startTime_ns.toDouble should equal(startTime_ns.toDouble +- startTime_ns * 0.1)
    delayedFuture.delay should equal(1.millis)
    delayedFuture.get should equal(1)
  }

  "Future.happensBefore" must "start a future after another completes" in {
    implicit val ctc = mkConcurrentTestContext()

    val latch1 = Latch()
    val latch2 = Latch()
    val deferredFuture = latch1.future happensBefore {
      latch2.future.map(_ => 1)
    }
    deferredFuture.isCompleted should equal(false)
    latch1.set()
    deferredFuture.deferred.get.isCompleted should equal(false)
    latch2.set()
    deferredFuture.get should equal(1)
  }

//  implicit class SMach_Concurrent_PimpMyTraversableFuture[A, M[+AA] <: Traversable[AA]](
//    val self: M[Future[A]]
//  ) extends AnyVal {
//    @inline def merge(implicit
//      ec: ExecutionContext,
//      cbf: CanBuildFrom[Nothing, A, M[A]]
//    ) : Future[M[A]] = MergeOps.merge(self)
//
//    @inline def merge(atMost: Duration)(implicit
//      ec: ExecutionContext,
//      ses: ScheduledExecutorService,
//      cbf: CanBuildFrom[Nothing, A, M[A]]
//    ) : Future[M[A]] = MergeOps.mergeTimeout(atMost, self)
//
//    @inline def firstSuccess(implicit
//      ec: ExecutionContext
//    ) : Future[A] = FutureOps.firstSuccess(self)
//  }
//
//  implicit class SMach_Concurrent_PimpMyTraversableFutureTraversable[
//    A,
//    M[+AA] <: Traversable[AA],
//    N[+AA] <: TraversableOnce[AA]
//  ](
//    val self: M[Future[N[A]]]
//  ) extends AnyVal {
//    @inline def flatMerge(implicit
//      ec: ExecutionContext,
//      cbf: CanBuildFrom[Nothing, A, M[A]]
//    ) : Future[M[A]] = MergeOps.flatMerge(self)
//  }
//
//  implicit class SMach_Concurrent_PimpMyTraversableOnceFuture[A,M[AA] <: TraversableOnce[AA]](
//    val self: M[Future[A]]
//  ) extends AnyVal {
//    @inline def mergeAllFailures(implicit
//      ec: ExecutionContext,
//      cbf: CanBuildFrom[M[Future[A]], Throwable, M[Throwable]]
//    ) : Future[M[Throwable]] = MergeOps.mergeAllFailures(self)
//
//    @inline def sequence(implicit
//      cbf: CanBuildFrom[M[Future[A]], A, M[A]],
//      ec: ExecutionContext
//    ) : Future[M[A]] = Future.sequence(self)
//  }
//

}
