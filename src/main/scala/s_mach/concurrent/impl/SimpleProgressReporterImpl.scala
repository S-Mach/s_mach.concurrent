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

import s_mach.concurrent.util.{SimpleProgressReporter, Progress}

class SimpleProgressReporterImpl(
  optTotal: Option[Int],
  report: Progress => Unit
) extends SimpleProgressReporter {
  val lock = new Object
  var totalSoFar = 0
  var startTime_ns = 0l

  override def onStartTask(): Unit = {
    lock.synchronized {
      totalSoFar = 0
      startTime_ns = System.nanoTime()
      report(Progress(0, optTotal, startTime_ns))
    }
  }


  override def onCompleteTask(): Unit = { }

  override def onStartStep(stepId: Int) = { }

  override def onCompleteStep(stepId: Int) : Unit = {
    // Note: lock is required here to ensure proper ordering of very fast reports
    lock.synchronized {
      totalSoFar += 1
      report(Progress(totalSoFar, optTotal, startTime_ns))
    }
  }
}
