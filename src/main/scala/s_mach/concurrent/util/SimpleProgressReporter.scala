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

/**
 * A trait for a simple progress reporter that accumulates total progress from completed reports and makes a report
 * for each completed report
 */
trait SimpleProgressReporter extends ProgressReporter

object SimpleProgressReporter {
  def apply(
    optTotal: Option[Long],
    report: Progress => Unit
  ) : SimpleProgressReporter = new SimpleProgressReporterImpl(
    optTotal = optTotal,
    report = report
  )

  class SimpleProgressReporterImpl(
    optTotal: Option[Long],
    report: Progress => Unit
  ) extends SimpleProgressReporter {
    val lock = new Object
    var totalSoFar = 0l
    var startTime_ns = 0l

    override def onStartTask(): Unit = {
      lock.synchronized {
        totalSoFar = 0
        startTime_ns = System.nanoTime()
        report(Progress(0, optTotal, startTime_ns))
      }
    }


    override def onCompleteTask(): Unit = { }

    override def onStartStep(stepId: Long) = { }

    def onCompleteStep(stepId: Long) : Unit = {
      // Note: lock is required here to ensure proper ordering of very fast reports
      lock.synchronized {
        totalSoFar += 1
        report(Progress(totalSoFar, optTotal, startTime_ns))
      }
    }
  }
}

