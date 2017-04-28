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

import scala.concurrent.{Future, ExecutionContext}
import s_mach.concurrent.impl.SequencerImpl
import s_mach.concurrent.DeferredFuture

/**
 * A trait used to guarantee a series of unordered tasks occur sequentially.
 * By associating a sequence number with each task, the sequencer can determine
 * whether to immediately run a task or queue the task for later. When a task
 * has been queued and the sequence number for that task has been reached, the
 * task is removed from the queue and executed. The sequence number is only
 * advanced after the task completes.
 * 
 * Note: it is assumed each task has a unique sequence number. A request to
 * execute a task with a sequence number that is less than the current sequence
 * number causes an IllegalArgumentException to be thrown.
 */
trait Sequencer {
  /** @return the next sequence number to be executed */
  def next : Int

  /**
   * @throws java.lang.IllegalArgumentException if sequenceNumber is less than next
   * @return a Future that completes once the sequence number has been reached
   *         and the task has completed
   * */
  def when[X](
    sequenceNumber: Int
  )(task: => Future[X])(implicit ec:ExecutionContext) : DeferredFuture[X]
}

object Sequencer {
  def apply(next: Int = 0) : Sequencer = new SequencerImpl(next)
}
