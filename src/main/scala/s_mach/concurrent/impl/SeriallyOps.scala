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
package s_mach.concurrent.impl

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object SeriallyOps extends SeriallyOps
trait SeriallyOps {
  /**
   * Transform Futures serially
   * Derived from: http://www.michaelpollmeier.com/execute-scala-futures-in-serial-one-after-the-other-non-blocking/
   * @return a Future of M[B] that completes once all Futures have been transformed serially
   */
  def mapSerially[A, B, M[AA] <: TraversableOnce[AA]](
    self: M[A],
    f: A => Future[B]
  )(implicit
    ec: ExecutionContext,
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = {
    val builder = cbf()
    if(self.isTraversableAgain) {
      builder.sizeHint(self.size)
    }
    self.foldLeft(Future.successful(builder)) { (fbuilder, a) =>
      for {
        builder <- fbuilder
        b <- f(a)
      } yield builder += b
    } map (_.result())
  }

  /**
   * Transform and flatten Futures serially
   * Derived from: http://www.michaelpollmeier.com/execute-scala-futures-in-serial-one-after-the-other-non-blocking/
   * @return a Future of M[B] that completes once all Futures have been transformed serially
   */
  def flatMapSerially[A, B, M[AA] <: TraversableOnce[AA]](
    self: M[A],
    f: A => Future[TraversableOnce[B]]
  )(implicit
    ec: ExecutionContext,
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = {
    val builder = cbf()
    if(self.isTraversableAgain) {
      builder.sizeHint(self.size)
    }
    self.foldLeft(Future.successful(builder)) { (fbuilder, a) =>
      for {
        builder <- fbuilder
        b <- f(a)
      } yield builder ++= b
    } map (_.result())
  }

  /**
   * Traverse Futures serially.
   * Derived from: http://www.michaelpollmeier.com/execute-scala-futures-in-serial-one-after-the-other-non-blocking/
   * @return a Future of M[B] that completes once all Futures have been traversed serially
   */
  def foreachSerially[A, U, M[AA] <: TraversableOnce[AA]](
    self: M[A],
    f: A => Future[U]
  )(implicit ec:
    ExecutionContext
  ) : Future[Unit] = {
    self.foldLeft(Future.successful(())) { (fut, a) =>
      for {
        _ <- fut
        b <- f(a)
      } yield ()
    }
  }

  /**
   * Fold left Futures serially
   * Derived from: http://www.michaelpollmeier.com/execute-scala-futures-in-serial-one-after-the-other-non-blocking/
   * @return a Future of M[B] that completes once all Futures have been fold left
   */
  def foldLeftSerially[A, B, M[AA] <: TraversableOnce[AA]](
    self: M[A],
    z: B,
    f: (B, A) => Future[B]
  )(implicit
    ec: ExecutionContext,
    cbf: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[B] = {
    val builder = cbf()
    if(self.isTraversableAgain) {
      builder.sizeHint(self.size)
    }
    self.foldLeft(Future.successful(z)) { (fB, a:A) =>
      for {
        b <- fB
        nextB <- f(b,a)
      } yield nextB
    }
  }

}
