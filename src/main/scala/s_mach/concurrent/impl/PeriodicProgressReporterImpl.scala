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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import s_mach.concurrent.{PeriodicTask, ScheduledExecutionContext}
import s_mach.concurrent.util.{AtomicFSM, Progress, PeriodicProgressReporter}
import s_mach.codetools._

object PeriodicProgressReporterImpl {
  sealed trait State
  object NotStarted extends State
  case class Running(
    startTime_ns: Long = System.nanoTime(),
    totalSoFar: Int = 0
  ) extends State
  case class Done(startTime_ns: Long, finalTotal: Int) extends State
}

class PeriodicProgressReporterImpl(
  optTotal: Option[Int],
  val reportInterval: FiniteDuration,
  report: Progress => Unit
)(implicit
  executionContext: ExecutionContext,
  scheduledExecutionContext: ScheduledExecutionContext
) extends PeriodicProgressReporter {
  import PeriodicProgressReporterImpl._

  val state = new AtomicFSM[State](NotStarted)

  override def onStartTask() : Unit = state(
    transition = {
      case NotStarted => Running()
    },
    onTransition = {
      case (NotStarted,Running(startTime_ns,_)) =>
        report(Progress(0, optTotal, startTime_ns))
        task.state match {
          case p:PeriodicTask.Paused => p.resume()
          case s => throw new IllegalStateException(s"Unexpected state $s")
        }
        ()
    }
  ).discard

  override def onStartStep(sequenceNumber: Int) : Unit = { }
  override def onCompleteStep(sequenceNumber: Int) : Unit = state {
    case current:Running =>
      import current._
      copy(totalSoFar = totalSoFar + 1)
  }.discard

  override def onCompleteTask() : Unit = state(
    transition = {
      case current:Running =>
        import current._
        Done(startTime_ns, totalSoFar)
    },
    onTransition = {
      case (Running(_,_),Done(startTime_ns, finalTotal)) =>
        task.cancel()
        report(Progress(finalTotal, optTotal, startTime_ns))
    }
  ).discard

  val task: PeriodicTask = {
    scheduledExecutionContext.scheduleAtFixedRate(
      initialDelay = reportInterval,
      period = reportInterval,
      paused = true
    ) { () =>
      state.get match {
        case r:Running =>
          import r._
          if(optTotal.forall(_ != totalSoFar)) {
            report(Progress(totalSoFar, optTotal, startTime_ns))
          }
        case d:Done =>
          // Note: there is a race condition between the periodic task hitting Done and the post-Done commit that
          // cancels this periodic task that isn't impactful
          task.cancel()
        case notRunning => throw new IllegalStateException(s"$notRunning")
      }
    }
  }

}
