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
package s_mach.concurrent.util

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future, TimeoutException}
import scala.util.Try

/**
 * A base trait for a future that delegates its implementation to another
 * delegate future
 */
trait DelegatedFuture[A] extends Future[A] {
  /** @return the future to delegate to */
  def delegate : Future[A]

  override final def onComplete[U](
    f: (Try[A]) => U
  )(implicit executor: ExecutionContext): Unit = delegate.onComplete(f)
  override final def isCompleted: Boolean = delegate.isCompleted
  override final def value: Option[Try[A]] = delegate.value
  @scala.throws[Exception](classOf[Exception])
  override final def result(atMost: Duration)(implicit permit: CanAwait): A =
    delegate.result(atMost)
  @scala.throws[InterruptedException](classOf[InterruptedException])
  @scala.throws[TimeoutException](classOf[TimeoutException])
  override final def ready(
    atMost: Duration
  )(implicit permit: CanAwait): this.type = {
    delegate.ready(atMost)
    this
  }
}
