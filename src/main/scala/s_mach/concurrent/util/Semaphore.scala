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
import s_mach.concurrent._

/**
 * A trait for a non-blocking semaphore that ensures limiting concurrent execution to tasks that have been granted
 * the requested number of permits from a limited pool of permits. The acquire method is used to request the execution
 * of a task once the requested number of permits become available. If the permits are not immediately available, the
 * request is placed into a FIFO queue while waiting for permits to become available. The permit pool size is typically
 * static with permits released back to the pool upon completion of tasks. However, some implementations may have
 * dynamic sized pools that expend permits instead of releasing them, replenishing permits through some other mechanism.
 *
 * Note1: Because Semaphore accepts a function to the task to run, it is not possible for callers to double lock, double 
 * release or forget to release the lock.
 * Note2: Semaphore is NOT reentrant
 * Note3: Semaphore should never be used when other options make more sense. Semaphore is designed for synchronizing
 * many tasks (100+) and/or long running tasks (100us+). Unlike other options, Semaphore does not block and consume a
 * thread while waiting for permits to become available. However, Semaphore is not the most performant option.
 */
trait Semaphore {
  /**
   * @param task the task to run once permits are available
   * @throws IllegalArgumentException if the number of requested permits exceeds maxAvailablePermits
   * @return a future that completes once permitCount permits are available AND task completes. task is started once
   *         permitCount permits are available. The permits are removed from the pool while task is running and after
   *         task completes the permits are returned to the pool.
   * */
  @inline final def acquire[X](permitCount: Long)(task: () => Future[X])(implicit ec:ExecutionContext) : Future[X] =
    acquireEx(permitCount)(task).flatten
  @inline final def apply[X](permitCount: Long)(task: () => Future[X])(implicit ec:ExecutionContext) : Future[X] =
    acquireEx(permitCount)(task).flatten

  // TODO: better name for this method
  /**
   * @param task the task to run once permits are available
   * @throws IllegalArgumentException if the number of requested permits exceeds maxAvailablePermits
   * @return a future that completes once permitCount permits are available. task is started once permitCount permits
   *         are available. The permits are removed from the pool while task is running and after task completes the
   *         permits are returned to the pool. The inner future is the task and completes once task completes.
   * */
  def acquireEx[X](perimitCount: Long)(task: () => Future[X])(implicit ec:ExecutionContext) : Future[Future[X]]

  /** @return the maximum number of permits in the pool */
  def maxAvailablePermits : Long
  /** @return the current number of permits available  */
  def availablePermits : Long
  /** @return the count of callers currently waiting on permits to become available */
  def waitQueueLength: Long
}

object Semaphore {
  /**
   * @throws IllegalArgumentException if permitCount is less than 1
   * @return a semaphore with a fixed size permit pool */
  def apply(permitCount: Long) : Semaphore = {
    require(permitCount >= 1)

    new SemaphoreImpl(permitCount) {
      override def maxAvailablePermits = permitCount
    }
  }

  /**
   * The default implementation of Semaphore using a ListBuffer backend. maxAvailablePermits is not defined and a hook
   * for making new permits available is provided.
   */
  abstract class SemaphoreImpl(
    initialOffering: Long
  ) extends Semaphore {
    @volatile private[this] var offering = initialOffering
    private[this] val lock = new Object
    private[this] val polling = new mutable.ListBuffer[(() => Unit, Long)]()

    override def waitQueueLength = lock.synchronized { polling.size }
    override def availablePermits = lock.synchronized { offering }
    protected def availablePermits(_availablePermits: Long) = lock.synchronized { offering = _availablePermits }

    protected def run[X](task: () => Future[X], permitCount: Long)(implicit ec: ExecutionContext) : Future[X] = {
      val retv = task()
      retv onComplete { case _ => replenish(permitCount) }
      retv
    }

    protected def replenish(permitCount: Long)(implicit ec:ExecutionContext) : Unit = {
      // Note: locking here isn't the fastest however not much is done here - tho I'm sure a better concurrent impl exists
      lock.synchronized {
        // Steal offering to local to avoid conflicts b/c locks are released between loops here
        val localOffering = permitCount + offering
        offering =
          if(polling.nonEmpty && localOffering >= polling.head._2) {
            val permitsRemaining = localOffering - polling.head._2
            polling.remove(0)._1()
            if(permitsRemaining > 0) {
              Future {
                replenish(permitsRemaining)
              }.background
            }
            0
          } else {
            localOffering
          }
      }
    }

    override def acquireEx[X](permitCount: Long)(task: () => Future[X])(implicit ec:ExecutionContext): Future[Future[X]] = {
      require(permitCount <= maxAvailablePermits)

      // Note: locking here isn't the fastest however not much is done here - tho I'm sure a better concurrent impl exists
      lock.synchronized {
        if(offering >= permitCount) {
          offering -= permitCount
          Future.successful(run(task, permitCount))
        } else {
          // Take as many permits as possible now, wait for the rest
          val missingPermitCount = permitCount - offering
          offering = 0
          val p = Promise[Future[X]]()
          polling += (
            (
              { () => p.success(run(task, permitCount)) },
              missingPermitCount
            )
          )
          p.future
        }
      }
    }
  }

}
