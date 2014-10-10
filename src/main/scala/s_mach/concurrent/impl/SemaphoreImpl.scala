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

import s_mach.concurrent.util.Semaphore

import scala.collection.mutable
import scala.concurrent.{Promise, ExecutionContext, Future}
import s_mach.concurrent._

/**
 * The default implementation of Semaphore using a ListBuffer backend.
 * maxAvailablePermits is not defined and a hook for making new permits
 * available is provided.
 */
abstract class SemaphoreImpl(
  initialOffering: Long
) extends Semaphore {
  @volatile private[this] var offering = initialOffering
  private[this] val lock = new Object
  private[this] val polling = new mutable.ListBuffer[(() => Unit, Long)]()

  override def waitQueueLength = lock.synchronized { polling.size }
  override def availablePermits = lock.synchronized { offering }
  protected def availablePermits(_availablePermits: Long) = lock.synchronized {
    offering = _availablePermits
  }

  protected def run[X](
    task: => Future[X],
    permitCount: Long
  )(implicit ec: ExecutionContext) : Future[X] = {
    val retv = task
    retv onComplete { case _ => replenish(permitCount) }
    retv
  }

  protected def replenish(
    permitCount: Long
  )(implicit ec:ExecutionContext) : Unit = {
    // Note: locking here isn't the fastest however not much is done here - tho
    // I'm sure a better concurrent impl exists
    lock.synchronized {
      // Steal offering to local to avoid conflicts b/c locks are released
      // between loops here
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

  override def acquire[X](
    permitCount: Long
  )(
    task: => Future[X]
  )(implicit ec:ExecutionContext): DeferredFuture[X] = {
    require(permitCount <= maxAvailablePermits)

    // Note: locking here isn't the fastest however not much is done here - tho
    // I'm sure a better concurrent impl exists
    lock.synchronized {
      if(offering >= permitCount) {
        offering -= permitCount
        DeferredFuture.successful(run(task, permitCount))
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
        DeferredFuture(p.future)
      }
    }
  }
}
