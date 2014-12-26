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

import scala.concurrent.{ExecutionContext, Future}

/**
 * A trait for a non-blocking queue.
 */
trait Queue[A] {
  /** @return a future that completes once input is available */
  def poll()(implicit ec:ExecutionContext) : Future[A]

  /** @return a future that completes once input is available */
  def poll(n: Int)(implicit ec:ExecutionContext) : Future[Vector[A]]

  /** Offer a value for waiting futures */
  def offer(a: A) : Unit

  /** Offer values for waiting futures */
  def offer(xa: TraversableOnce[A]) : Unit

  /** @return size of offer queue */
  def offerQueueSize: Int

  /** @return size of polling queue */
  def pollQueueSize: Int
}
