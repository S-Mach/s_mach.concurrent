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

import scala.concurrent.Future

/**
 * A trait for capturing all Throwables thrown during a concurrent operation. The first Throwable is immediately
 * available and all others can be retrieved by waiting on a future of all Throwables.
 */
trait ConcurrentThrowable extends Throwable {
  /** @return the first failure thrown during concurrent processing */
  def firstFailure: Throwable
  /** @return a Future of all failures thrown during concurrent processing */
  def allFailure: Future[Vector[Throwable]]
}

object ConcurrentThrowable {
  def apply(
    firstFailure: Throwable,
    allFailure: => Future[Vector[Throwable]] = Future.successful(Vector.empty)
  ) : ConcurrentThrowable = {
    val _firstFailure = firstFailure
    lazy val _allFailure = allFailure
    new ConcurrentThrowable {
      override def firstFailure = _firstFailure
      override def allFailure = _allFailure
      
      override def getMessage: String = firstFailure.getMessage
      override def getLocalizedMessage: String = firstFailure.getLocalizedMessage
      override def getCause: Throwable = firstFailure
      override def initCause(cause: Throwable): Throwable = throw new UnsupportedOperationException
      override def toString: String = s"ConcurrentThrowable(${firstFailure.toString})"
    }
  }
}