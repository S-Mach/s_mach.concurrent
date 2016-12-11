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
package s_mach.concurrent.util

import s_mach.concurrent.DeferredFuture

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.impl.SemaphoreImpl

/**
 * A trait for a non-blocking semaphore that ensures limiting concurrent
 * execution to tasks that have been granted the requested number of permits
 * from a limited pool of permits. The acquire method is used to request the
 * execution of a task once the requested number of permits become available. If
 * the permits are not immediately available, the request is placed into a FIFO
 * queue while waiting for permits to become available. The permit pool size is
 * typically static with permits released back to the pool upon completion of
 * tasks. However, some implementations may have dynamic sized pools that expend
 * permits instead of releasing them, replenishing permits through some other
 * mechanism.
 *
 * Note1: Because Semaphore accepts a function to the task to run, it is not
 * possible for callers to double lock, double  release or forget to release
 * permits.
 * Note2: Semaphore is NOT reentrant
 * Note3: Semaphore should never be used when other options make more sense.
 * Semaphore is designed for synchronizing many tasks (100+) and/or long running
 * tasks (10ms+). Unlike other options, Semaphore does not block and consume a
 * thread while waiting for permits to become available. However, Semaphore is
 * not the most performant option.
 */
trait Semaphore {
  /**
   * @param task the task to run once permits are available
   * @throws java.lang.IllegalArgumentException if the number of requested permits
             exceeds maxAvailablePermits
   * @return a future that completes once permitCount permits are available AND
   *         task completes. task is started once permitCount permits are
   *         available. The permits are removed from the pool while task is
   *         running and after task completes the permits are returned to the
   *         pool.
   * */
  def acquire[X](
    permitCount: Int
  )(
    task: => Future[X]
  )(implicit ec:ExecutionContext) : DeferredFuture[X]

  /** @return the maximum number of permits in the pool */
  def maxAvailablePermits : Int
  /** @return the current number of permits available  */
  def availablePermits : Int
  /** @return the count of callers currently waiting on permits to become
    *         available */
  def waitQueueLength: Int
}

object Semaphore {
  /**
   * @throws java.lang.IllegalArgumentException if permitCount is less than 1
   * @return a semaphore with a fixed size permit pool */
  def apply(permitCount: Int) : Semaphore = {
    require(permitCount >= 1)

    new SemaphoreImpl(permitCount) {
      override def maxAvailablePermits = permitCount
    }
  }
}
