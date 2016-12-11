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

import s_mach.concurrent.DeferredFuture

import scala.collection.mutable
import scala.concurrent.{Promise, Future, ExecutionContext}
import s_mach.concurrent.util.Sequencer
import s_mach.codetools._

class SequencerImpl(__next: Int) extends Sequencer {
  type Task = () => Unit
  val lock = new Object

  var _next = __next
  override def next = lock.synchronized { _next }

  val queOrdering = new Ordering[(Int, Task)] {
    override def compare(x: (Int, Task), y: (Int, Task)) =
      // Note: inverted x and y compare here to make lowest index =
      // highest priority
      implicitly[Ordering[Int]].compare(y._1,x._1)
  }
  val polling = mutable.PriorityQueue[(Int, Task)]()(queOrdering)

  def doNext()(implicit ec:ExecutionContext) : Unit = {
    lock.synchronized {
      _next = _next + 1
      if(polling.nonEmpty && _next == polling.head._1) {
        polling.dequeue()._2.apply()
      }
    }
  }

  def run[X](task: => Future[X])(implicit ec:ExecutionContext) : Future[X] = {
    val retv = task
    retv onComplete { case _ => doNext() }
    retv
  }

  override def when[X](
    i: Int
  )(task: => Future[X])(implicit ec: ExecutionContext): DeferredFuture[X] = {
    lock.synchronized {
      require(i >= _next)

      if(_next == i) {
        DeferredFuture.successful(run(task))
      } else {
        val promise = Promise[Future[X]]()
        polling.enqueue((i, { () => promise.success(run(task)).discard }))
        DeferredFuture(promise.future)
      }
    }
  }

}
