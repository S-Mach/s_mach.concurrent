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

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

object ConcurrentlyOps extends ConcurrentlyOps
trait ConcurrentlyOps {
  /**
   * Transform Futures concurrently
   * @return a Future of M[B] that completes once all Futures have been transformed concurrently
   */
  @inline def mapConcurrently[A, B, M[AA] <: Traversable[AA]](
    self: M[A],
    f: A => Future[B]
  )(implicit
    ec: ExecutionContext,
    cbf1: CanBuildFrom[Nothing, Future[B], M[Future[B]]],
    cbf2: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = {
    val futures = self.map[Future[B], M[Future[B]]](f)(scala.collection.breakOut(cbf1))
    MergeOps.merge(futures)
  }

  /**
   * Transform and flatten Futures concurrently
   * @return a Future of M[B] that completes once all Futures have been transformed concurrently
   */
  @inline def flatMapConcurrently[A, B, M[AA] <: Traversable[AA]](
    self: M[A],
    f: A => Future[TraversableOnce[B]]
  )(implicit
    ec: ExecutionContext,
    cbf1: CanBuildFrom[Nothing, Future[TraversableOnce[B]], M[Future[TraversableOnce[B]]]],
    cbf2: CanBuildFrom[Nothing, B, M[B]]
  ) : Future[M[B]] = {
    val futures = self.map[Future[TraversableOnce[B]], M[Future[TraversableOnce[B]]]](f)(scala.collection.breakOut(cbf1))
    MergeOps.flatMerge(futures)
  }

  /**
   * Traverse Futures concurrently
   * @return a Future of M[B] that completes once all Futures have been traversed concurrently
   */
  @inline def foreachConcurrently[A, U, M[AA] <: Traversable[AA]](
    self: M[A],
    f: A => Future[U]
  )(implicit
    ec: ExecutionContext,
    cbf1: CanBuildFrom[Nothing, Future[U], M[Future[U]]],
    cbf2: CanBuildFrom[Nothing, U, M[U]]
  ) : Future[Unit] = {
    mapConcurrently(self, f).map(_ => ())
  }

}
