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
package s_mach.concurrent.util

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent._

trait DeferredFuture[A] extends Future[A] {
  def deferred : Future[Future[A]]
}

object DeferredFuture {
  case class DeferredFutureImpl[A](
    deferred: Future[Future[A]]
  )(implicit ec:ExecutionContext) extends DeferredFuture[A] with DelegatedFuture[A] {
    def delegate = deferred.flatten
  }

  def successful[A](future: Future[A]) : DeferredFuture[A] = new DeferredFuture[A] with DelegatedFuture[A] {
    override def deferred: Future[Future[A]] = Future.successful(future)
    override def delegate: Future[A] = future
  }

  def apply[A](
    deferred: Future[Future[A]]
  )(implicit ec: ExecutionContext) : DeferredFuture[A] = DeferredFutureImpl(deferred)
}
