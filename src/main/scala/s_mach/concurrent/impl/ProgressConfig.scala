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

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import s_mach.concurrent._
import s_mach.concurrent.util.{SimpleProgressReporter, Progress, PeriodicProgressReporter, ProgressReporter}

/**
 * A trait for a builder of ProgressConfig. Callers may set the optional progress reporting function by calling one of
 * the progress methods. If the progress reporting function is never called then the optional progress function is left
 * unset.
 * @tparam MDT most derived type
 */
trait ProgressConfigBuilder[MDT <: ProgressConfigBuilder[MDT]] {

  /** The optional total number of operations as set by derived type */
  def optTotal : Option[Long]

  /**
   * Set the optional progress reporting function.
   * @param reporter a function that accepts the number of completed operations (typically this is 1) as they occur
   * @return a copy of the builder with the new setting
   */
  def progress(reporter: ProgressReporter) : MDT

  /**
   * Set the progress reporting function
   * @param report a function that accepts the number of operations completed and the total number of operations (or 0
   *               if the total is unknown)
   * @return a copy of the builder with the new setting
   */
  def progress(report: Progress => Unit) : MDT = progress(SimpleProgressReporter(optTotal, report))

  /**
   * Set the progress reporting function to periodically report progress
   * @param reportInterval the interval to report progress
   * @param report a function that accepts the number of operations completed and the total number of operations (or 0
   *               if the total is unknown)
   * @return a copy of the builder with the new setting
   */
  def progress(reportInterval: Duration)(report: Progress => Unit)(implicit
    executionContext: ExecutionContext,
    scheduledExecutionContext: ScheduledExecutionContext
  ) : MDT = {
    progress(PeriodicProgressReporter(optTotal, reportInterval, report))
  }

  /** @return a ProgressConfig with the optional progress function */
  def build : ProgressConfig
}

/**
 * A trait for a concurrent function builder that can add progress reporting to a concurrent function
 */
trait ProgressConfig extends ConcurrentFunctionBuilder with LoopConfig {
  implicit def executionContext: ExecutionContext

  /** @return the optional progress reporting function */
  def optProgress : Option[ProgressReporter]


  override def onLoopStart(): Unit = {
    super.onLoopStart()
    optProgress.foreach(_.onStartTask())
  }

  override def onLoopEnd(): Unit = {
    super.onLoopEnd()
    optProgress.foreach(_.onCompleteTask())
  }

  /** @return if progress is set, a new function that reports progress after each returned Future completes. Otherwise,
    *         the function unmodified */
  override def build2[A,B](f: A => Future[B]) : A => Future[B] = {
    super.build2 {
      optProgress match {
        case Some(progress) =>
          // TODO: better way to do this?
          val nextStepId = new java.util.concurrent.atomic.AtomicLong(0)

          { a:A =>
            val stepId = nextStepId.getAndIncrement()
            progress.onStartStep(stepId)
            val promise = Promise[B]()
            f(a) onComplete {
              case f@Failure(_) =>
                // Don't report progress on failure
                promise.complete(f)
              case s@Success(_) =>
                // Note: need to ensure progress happens before next iteration
                progress.onCompleteStep(stepId)
                promise.complete(s)
            }
            promise.future
          }
        case None => f
      }
    }
  }

  /** @return if progress is set, a new function that reports progress after each returned Future completes. Otherwise,
    *         the function unmodified */
  override def build3[A,B,C](f: (A,B) => Future[C]) : (A,B) => Future[C] = {
    super.build3 {
      optProgress match {
        case Some(progress) =>
          // TODO: better way to do this?
          val nextStepId = new java.util.concurrent.atomic.AtomicLong(0)

          { (a:A,b:B) =>
            val stepId = nextStepId.getAndIncrement()
            progress.onStartStep(stepId)
            val promise = Promise[C]()
            f(a,b) onComplete {
              case f@Failure(_) =>
                // Don't report progress on failure
                promise.complete(f)
              case s@Success(_) =>
                // Note: need to ensure progress happens before next iteration
                progress.onCompleteStep(stepId)
            }
            promise.future
          }
        case None => f
      }
    }
  }

}