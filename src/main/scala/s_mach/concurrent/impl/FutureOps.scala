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
package s_mach.concurrent.impl


import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Try,Success,Failure}
import MergeOps._
import s_mach.concurrent._
import s_mach.codetools._

object FutureOps extends FutureOps
trait FutureOps {

  val _unit = Future.successful(())
  @inline def unit : Future[Unit] = _unit

  /**
   * @return the result of the Future after it completes (Note: this waits
   * indefinitely for the Future to complete)
   * @throws java.lang.Exception Future completed with a failure, throws the exception
   * */
  @inline def get[A](self: Future[A]): A = {
    Await.result(self, Duration.Inf)
  }

  /**
   * @return the result of the Future after it completes
   * @throws java.util.concurrent.TimeoutException if Future does not complete within max duration
   * */
  @inline def get[A](self: Future[A], max: Duration): A =
    Await.result(self, max)

  /**
   * @return the Try result of the Future after it completes (Note: this waits
   * indefinitely for the Future to complete)
   * */
  @inline def getTry[A](self: Future[A]): Try[A] = {
    Await.ready(self, Duration.Inf).value.get
  }

  /**
   * @return the Try result of the Future after it completes
   * @throws java.util.concurrent.TimeoutException if Future does not complete within max duration
   * */
  @inline def getTry[A](self: Future[A], max: Duration): Try[A] = {
    Await.ready(self, max).value.get
  }

  /** Run future in the background. Discard the result of this Future but
    * ensure if there is an exception it gets reported to the ExecutionContext
    * */
  @inline def background[A](
    self: Future[A]
  )(implicit ec: ExecutionContext) : Unit = {
    self onComplete {
      case Failure(throwable) => ec.reportFailure(throwable)
      case _ =>
    }
  }

  /** @return a Future of a Try of the result that always completes
    * successfully even if the Future eventually throws an exception
    * */
  def toTry[A](
    self: Future[A]
  )(implicit ec: ExecutionContext): Future[Try[A]] = {
    val p = Promise[Try[A]]()
    self onComplete { case result => p.success(result) }
    p.future
  }

  /** @return a Future of X that always succeeds. If self is successful, X
    * is derived from onSuccess otherwise if self is a failure, X is derived
    * from onFailure.
    * */
  def fold[A, X](
    self: Future[A],
    onSuccess: A => X,
    onFailure: Throwable => X
  )(implicit
    ec: ExecutionContext
  ) : Future[X] = {
    val p = Promise[X]()
    self onComplete {
      case Success(v) => p.success(onSuccess(v))
      case Failure(t) => p.success(onFailure(t))
    }
    p.future
  }

  /** @return a Future of X that always succeeds. If self is successful, X
    * is derived from onSuccess otherwise if self is a failure, X is derived
    * from onFailure.
    * */
  def flatFold[A, X](
    self: Future[A],
    onSuccess: A => Future[X],
    onFailure: Throwable => Future[X]
  )(implicit
    ec: ExecutionContext
  ) : Future[X] = {
    val p = Promise[X]()
    self onComplete {
      case Success(v) => p.completeWith(onSuccess(v))
      case Failure(t) => p.completeWith(onFailure(t))
    }
    p.future
  }

  /**
   * @return the first successfully completed future. If all futures fail,
   * then completes the future with AsyncParThrowable of all failures.
   */
  def firstSuccess[A](
    xa: Traversable[Future[A]]
  )(implicit ec:ExecutionContext) : Future[A] = {
    val promise = Promise[A]()
    // First success completes the promise
    xa.foreach { fa =>
      fa foreach { a => promise.trySuccess(a) }
    }
    // If all futures fail, then complete with AsyncParThrowable
    mergeAllFailures(xa) foreach { allFailure =>
      if(allFailure.nonEmpty) {
        promise.tryFailure(
          AsyncParThrowable(
            allFailure.head,
            Future.successful(allFailure.toVector)
          )
        )
      }
    }
    promise.future
  }

  /** @return after spinning the current thread for delay_ns, returns the
    * error between the requested delay and the actual delay. Use this function
    * only when a precise delay is more important than overall performance.
    * */
  def nanoSpinDelay(delay_ns: Long) : Long = {
    if(delay_ns > 0) {
      val startTime_ns = System.nanoTime()
      val stopTime_ns = startTime_ns + delay_ns
      while (stopTime_ns >= System.nanoTime()) {}
      val actualDelay_ns = System.nanoTime() - startTime_ns
      actualDelay_ns - delay_ns
    } else {
      0
    }
  }


  /** @return a future of A that is guaranteed to happen before lhs */
  def happensBefore[A](
    lhs: Future[Any],
    rhs: => Future[A]
  )(implicit ec: ExecutionContext) : DeferredFuture[A] = {
    val promise = Promise[Future[A]]()
    lhs onComplete { case _ => promise.completeWith(Future.successful(rhs)) }
    DeferredFuture(promise.future)
  }

  /** @return a future that completes once the supplied Future and the supplied
    *         side effect complete. The side effect runs after the supplied
    *         future completes even if it fails.
    * */
  def sideEffect[A](
    self: Future[A],
    sideEffect: => Unit
  )(implicit ec:ExecutionContext) : Future[A] = {
    val promise = Promise[A]()
    // Note: it is important here that the side effect execute before completing
    // the promise
    self onComplete { case _try =>
      sideEffect
      promise.complete(_try)
    }
    promise.future
  }

  /** @return a future that completes with fallback if the specified timeout is
    *         exceeded, otherwise the completed result of the future */
  def onTimeout[A](
    self: Future[A],
    timeout: FiniteDuration
  )(
    fallback: => Future[A]
  )(implicit
    ec:ExecutionContext,
    sec:ScheduledExecutionContext
  ) : Future[A] = {
    // Using promise of future to ensure that once timeout is reached it is not
    // possible for self to complete the returned future even if fallback takes
    // awhile to complete itself
    val promise = Promise[Future[A]]()
    val futTimeout = sec.scheduleCancellable(timeout, ())(promise.trySuccess(fallback).discard)
    self onComplete {
      case Success(_) =>
        if(promise.trySuccess(self)) {
          futTimeout.cancel()
        }
      case Failure(t) =>
        // Ensure failures are always reported even after timeout
        if(promise.trySuccess(self) == false) {
          ec.reportFailure(t)
        } else {
          futTimeout.cancel()
        }
    }
    promise.future.flatMap(v => v)
  }

//  def compareAndSet[A](self: AtomicReference[A], f: A => A) : A = {
//    @tailrec def loop(): A = {
//      val expect = self.get
//      val newValue = f(expect)
//      if(self.compareAndSet(expect, newValue)) {
//        newValue
//      } else {
//        loop()
//      }
//    }
//    loop()
//  }
//
//  def tryCompareAndSet[A](self: AtomicReference[A], pf: PartialFunction[A,A]) : Option[A] = {
//    val opf = pf.lift
//    @tailrec def loop(): Option[A] = {
//      val expect = self.get
//      opf(expect) match {
//        case Some(newValue) =>
//          if(self.compareAndSet(expect, newValue)) {
//            Some(newValue)
//          } else {
//            loop()
//          }
//        case None => None
//      }
//    }
//    loop()
//  }


}

