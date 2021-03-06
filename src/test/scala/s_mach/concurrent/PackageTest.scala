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
package s_mach.concurrent

import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent._
import scala.concurrent.duration._
import s_mach.concurrent.util.Latch
import s_mach.concurrent.impl._
import TestBuilder._
import scala.util.{Failure, Success}

class PackageTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "package concurrent" must "provide convenience methods to various implementations" in {
    implicit val ctc = mkConcurrentTestContext()

    1.future shouldBe a[Future[_]]
    1.future.await should equal(1)

    Future.unit shouldBe a [Future[_]]
    Future.unit.await should equal(())

    val latch = Latch()
    an [TimeoutException] should be thrownBy latch.future.await(1.nanos)
    an [TimeoutException] should be thrownBy latch.future.awaitTry(1.nanos)

    val ex = new RuntimeException
    Future.successful(1).awaitTry should equal(Success(1))
    Future.failed(ex).awaitTry should equal(Failure(ex))

    Future.successful(1).toTry.await should equal(Success(1))
    Future.failed(ex).toTry.await should equal(Failure(ex))

    Future.successful(1).fold(
      onSuccess = { i:Int => i.toString },
      onFailure = { t:Throwable => t.toString }
    ).await should equal("1")

    Future.failed(ex).fold(
      onSuccess = { i:Int => i.toString },
      onFailure = { t:Throwable => t.toString }
    ).await should equal("java.lang.RuntimeException")

    Future.successful(1).flatFold(
      onSuccess = { i:Int => i.toString.future },
      onFailure = { t:Throwable => t.toString.future }
    ).await should equal("1")

    Future.failed(ex).flatFold(
      onSuccess = { i:Int => i.toString.future },
      onFailure = { t:Throwable => t.toString.future }
    ).await should equal("java.lang.RuntimeException")

    Future.successful(Future.successful(1)).flatten.await should equal(1)
    Future.successful(Future.failed(ex)).flatten.awaitTry should equal(Failure(ex))
    Future.failed(ex).flatten.awaitTry should equal(Failure(ex))

    val items = Vector(1,2,3)
    items.async should equal(CollectionAsyncTaskRunner(items))
    items.async.par should equal(ParCollectionAsyncTaskRunner(items))
    items.async.par(1) should equal(ParCollectionAsyncTaskRunner(items,1))
  }

  "Future.sideEffect" must "execute the side effect after the future completes" in {
    implicit val ctc = mkConcurrentTestContext()
    val ex = new RuntimeException
    val sideEffects = List.newBuilder[String]

    Future.successful(1).sideEffect { sideEffects += "1"; () }
    Future.failed(ex).sideEffect { sideEffects += "1"; () }

    ctc.waitForActiveExecutionCount(0)
    sideEffects.result() should equal(List("1","1"))
  }

  "Future.onTimeout" must "complete normally if timeout is not exceeded" in {
    implicit val ctc = mkConcurrentTestContext()

    val startTime_ns = System.nanoTime()
    val future = Future { 1 }.onTimeout(10.seconds)(Future.failed(new TimeoutException))

    ctc.waitForActiveExecutionCount(0)

    val elapsed = (System.nanoTime() - startTime_ns).nanos

    future.await should equal(1)
    elapsed should be < 1.second
  }

  "Future.onTimeout" must "fallback if timeout is exceeded" in {
    implicit val ctc = mkConcurrentTestContext()

    val ex = new TimeoutException
    val latch = Latch()
    val future = latch.future.onTimeout(1.nano)(Future.failed(ex))

    ctc.waitForActiveExecutionCount(0)

    future.awaitTry should equal(Failure(ex))
  }

  "Future.onTimeout" must "fallback if timeout is exceeded even if fallback takes awhile" in {
    implicit val ctc = mkConcurrentTestContext()

    val latch = Latch()
    val p = Promise[Int]()

    val future = p.future.onTimeout(1.nano)(Future {
      latch.spinUntilSet()
      1
    })
    Thread.sleep(1)
    latch.set()
    ctc.waitForActiveExecutionCount(0)

    future.awaitTry should equal(Success(1))
  }

  "Future.background" must "ensure exceptions are reported to ExecutionContext" in {
    val ctc = mkConcurrentTestContext()
    val failures = List.newBuilder[Throwable]
    implicit val ec = new ExecutionContext {
      override def reportFailure(cause: Throwable) = { failures += cause; () }
      override def execute(runnable: Runnable): Unit = ctc.execute(runnable)
    }
    val ex = new RuntimeException
    Future { throw ex }.runInBackground
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
    delayedFuture.await should equal(1)
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
    deferredFuture.deferred.await.isCompleted should equal(false)
    latch2.set()
    deferredFuture.await should equal(1)
  }

  "firstSuccess-t1" must "return the first future to successfully complete" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val endLatch = Latch()

      val f3 = Future { throw new RuntimeException }
      val f2 = f3 happensBefore Future { 2 }
      // Note: using endLatch here to prevent race condition between firstSuccess completing and f1 being triggered
      val f1 = endLatch happensBefore Future { 1 }

      val result = Vector(f1,f2,f3).firstSuccess

      waitForActiveExecutionCount(0)
      endLatch.set()

      result.awaitTry should equal(Success(2))
    }
  }

  "firstSuccess(fail)-t2" must "complete with a failure if all futures fail" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val f1 = Future { throw new RuntimeException("1") }
      val f2 = Future { throw new RuntimeException("2") }
      val f3 = Future { throw new RuntimeException("3") }

      val result = Vector(f1,f2,f3).firstSuccess

      result.awaitTry shouldBe a [Failure[_]]
    }
  }

  "AtomicReference.casLoopSet" must "set the value atomically and call after transition exactly once" in {
    val a = new java.util.concurrent.atomic.AtomicReference[String]("initial")
    var count = 0
    a.casLoopSet(_ + "1") {
      case ("initial","initial1") =>
        count = count + 1
        true
      case _ => false
    } shouldBe true
    count shouldBe 1
    a.get should equal("initial1")
  }

  "AtomicReference.casLoopMaybeSet" must "set the value atomically and call after transition exactly once" in {
    val a = new java.util.concurrent.atomic.AtomicReference[String]("initial")
    a.casLoopMaybeSet {
      case "initial" => "initial1"
    } {
      case ("initial","initial1") => true
      case _ => false
    } shouldBe true
    a.get should equal("initial1")
  }

  "AtomicReference.casLoopMaybeSet" must "not set the value if undefined but still call after transition exactly once" in {
    val a = new java.util.concurrent.atomic.AtomicReference[String]("initial")
    a.casLoopMaybeSet {
      case "nomatch" => "initial1"
    } {
      case ("initial","initial1") => true
      case _ => false
    } shouldBe false
    a.get should equal("initial")
  }

  "AtomicReference.casLoopFold" must "set the value atomically and call after transition exactly once" in {
    val a = new java.util.concurrent.atomic.AtomicReference[String]("initial")
    a.casLoopFold[String,Boolean] {
      case ("initial","1") => "initial1"
      case (s,_) => s
    } {
      case ("initial","initial1") => true
      case _ => false
    } ("1") shouldBe true
    a.get should equal("initial1")
  }
}
