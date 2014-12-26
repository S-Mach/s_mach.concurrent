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
package s_mach.concurrent

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.impl.DeferredFutureImpl

/**
 * A trait for a future whose execution does not begin until some condition
 * occurs. DeferredFuture completes once the start condition occurs and the
 * deferred future completes.
 */
trait DeferredFuture[A] extends Future[A] {
  /** @return a future that completes once the start condition occurs. The
   * future contains the deferred future (which is now running) */
  def deferred : Future[Future[A]]

  /** @return TRUE if the future has been started */
  def isStarted : Boolean = deferred.isCompleted
}

object DeferredFuture {
  def successful[A](
    future: Future[A]
  )(implicit ec:ExecutionContext) : DeferredFuture[A] =
    DeferredFutureImpl(Future.successful(future))

  def failed(
    cause: Throwable
  )(implicit ec:ExecutionContext) : DeferredFuture[Nothing] =
    DeferredFutureImpl(Future.failed(cause))

  def apply[A](
    deferred: Future[Future[A]]
  )(implicit ec: ExecutionContext) : DeferredFuture[A] =
    DeferredFutureImpl(deferred)
}
