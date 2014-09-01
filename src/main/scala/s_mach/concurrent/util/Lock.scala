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
import s_mach.concurrent.impl.LockImpl

/**
 * A trait for a non-blocking lock that ensures a synchronous execution schedule for all callers. A caller calls the
 * lock method to execute a task synchronously once the lock is available. Simultaneous tasks are placed in a FIFO queue 
 * while awaiting execution.
 *
 * Note1: Because Lock accepts a function to the task to run, it is not possible for callers to double lock, double 
 * release or forget to release the lock.
 * Note2: Lock is NOT reentrant
 * Note3: Lock should never be used when other locking options make more sense. Lock is designed for synchronizing many
 * tasks (100+) and/or long running tasks (10ms+). Unlike other locking options, Lock does not block and consume
 * a thread while waiting for the lock to become available. However, Lock is not the most performant option.
 */
trait Lock {
  /** @return a future that completes once the lock is available AND task completes. The lock is locked while task is
    *         running and after task completes it is unlocked.
   */
  def lock[X](task: => Future[X])(implicit ec:ExecutionContext) : DeferredFuture[X]

  /** @return TRUE if the lock is available */
  def isUnlocked : Boolean
  /** @return the count of callers waiting on the lock to become available */
  def waitQueueLength: Long
}

object Lock {
  def apply() : Lock = new LockImpl()
}
