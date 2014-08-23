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
import scala.concurrent.{Promise, Future, ExecutionContext}
import s_mach.concurrent._

/**
 * A trait used to guarantee a series of unordered tasks occur sequentially, by associating a sequential number with
 * each task i.e. task N completes before task N + 1 is started. The sequencer uses a sequence number to determine
 * whether to immediately run a task or queue the task for later. When a task has been queued and the sequence number
 * for that task has been reached, the task is removed from the queue and executed. The sequence number is only advanced
 * after the task completes.
 * 
 * Note: it is assumed each task has a unique sequence number. A request to execute a task with a sequence number that
 * is less than the current sequence number causes an IllegalArgumentException to be thrown.
 */
trait Sequencer {
  /** @return the next sequence number to be executed */
  def next : Int

  /**
   * @throws IllegalArgumentException if sequenceNumber is less than next
   * @return a Future that completes once the sequence number has been reached and the task has completed
   * */
  def when[X](sequenceNumber: Int)(task: () => Future[X])(implicit ec:ExecutionContext) : Future[X]
  @inline final def apply[X](sequenceNumber: Int)(task: () => Future[X])(implicit ec:ExecutionContext) : Future[X] =
    when(sequenceNumber)(task)
}

object Sequencer {
  class SequencerImpl(__next: Int) extends Sequencer {
    type Task = () => Unit
    private[this] val lock = new Object

    private[this] var _next = __next
    override def next = lock.synchronized { _next }

    private[this] val polling = mutable.PriorityQueue[(Int, Task)]()(new Ordering[(Int, Task)] {
      override def compare(x: (Int, Task), y: (Int, Task)) =
        // Note: inverted x and y compare here to make lowest index = highest priority
        implicitly[Ordering[Int]].compare(y._1,x._1)
    })
    
    private[this] def doNext()(implicit ec:ExecutionContext) {
      lock.synchronized {
        _next = _next + 1
        if(polling.nonEmpty && _next == polling.head._1) {
          polling.dequeue()._2.apply()
        }
      }
    }

    private[this] def run[X](task: () => Future[X])(implicit ec:ExecutionContext) : Future[X] = {
      val retv = task()
      retv onComplete { case _ => doNext() }
      retv
    }

    override def when[X](i: Int)(task: () => Future[X])(implicit ec: ExecutionContext): Future[X] = {
      lock.synchronized {
        require(i >= _next)

        if(_next == i) {
          run(task)
        } else {
          val promise = Promise[X]()
          polling.enqueue((i, { () => promise.completeWith(run(task)) }))
          promise.future
        }
      }
    }

  }

  def apply(next: Int = 0) : Sequencer = new SequencerImpl(next)
}
