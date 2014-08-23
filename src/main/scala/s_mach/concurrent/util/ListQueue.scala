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

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * An implementation of ConcurrentQueue using a ListBuffer backend
 * TODO: explore using a true "concurrent" back end collection that avoids locking (http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html)
 * @tparam A
 */
class ListQueue[A](elems: A*) extends Queue[A] {
  private[this] val offering = {
    val list = new mutable.ListBuffer[A]()
    list ++= elems
    list
  }
  private[this] val polling = new mutable.ListBuffer[Either[Promise[A], (Promise[Vector[A]], Int)]]()
  private[this] val lock = new Object

  override def offerQueueSize = lock.synchronized { offering.size }

  override def pollQueueSize = lock.synchronized {
    polling.map {
      case Left(_) => 1
      case Right(tuple) => tuple._2
    }.sum
  }

  override def poll()(implicit ec:ExecutionContext): Future[A] = {
    lock.synchronized {
      if(offering.nonEmpty) {
        Future.successful(offering.remove(0))
      } else {
        val p = Promise[A]()
        polling += Left(p)
        p.future
      }
    }
  }

  override def poll(n: Int)(implicit ec:ExecutionContext): Future[Vector[A]] = {
    lock.synchronized {
      if(offering.size >= n) {
        val retv = offering.iterator.take(n).toVector
        offering.remove(0, n)
        Future.successful(retv)
      } else {
        val p = Promise[Vector[A]]()
        polling += Right((p, n))
        p.future
      }
    }
  }

  private[this] def doOffer(a: A) : Unit = {
//    polling.headOption match {
//      case Some(Left(p)) =>
//        p.success(a)
//        polling.remove(0)
//      case Some(Right((p, size))) if size == offering.size + 1 =>
//        offering += a
//        p.success(offering.iterator.take(size).toVector)
//        offering.remove(0,size)
//        polling.remove(0)
//      case _ => offering += a
//    }
    // Note: not using match above for now for speed up (this code is expected to have high call rate)
    if(polling.nonEmpty) {
      if(polling.head.isLeft) {
        val p = polling.head.left.get
        p.success(a)
        polling.remove(0)
      } else {
        val (p, size) = polling.head.right.get
        offering += a
        if(size <= offering.size) {
          p.success(offering.iterator.take(size).toVector)
          offering.remove(0,size)
          polling.remove(0)
        }
      }
    } else {
      offering += a
    }
  }

  override def offer(a: A): Unit = {
    lock.synchronized { doOffer(a )}
  }

  override def offer(xa: TraversableOnce[A]): Unit = {
    // TODO: better algo that can take advantage of offering multiple items at once
    lock.synchronized { xa.foreach(doOffer) }
  }

}

object ListQueue {
  def apply[A](elems: A*) : ListQueue[A] = new ListQueue[A](elems:_*)
}