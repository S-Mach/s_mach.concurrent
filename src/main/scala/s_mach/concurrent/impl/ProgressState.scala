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

import s_mach.concurrent.config.ProgressConfig

import scala.concurrent.ExecutionContext
import s_mach.concurrent.util.TaskEventListener

case class ProgressState(
  reporter: TaskEventListener
)(implicit
  executionContext: ExecutionContext
) extends TaskEventListenerHook {
  override def onStartTask() = reporter.onStartTask()
  override def onCompleteTask() = reporter.onCompleteTask()
  override def onStartStep(stepId: Int) = reporter.onStartStep(stepId)
  override def onCompleteStep(stepId: Int) = reporter.onCompleteStep(stepId)
}

object ProgressState {
  def apply(cfg: ProgressConfig) : ProgressState = ProgressState(cfg.reporter)(cfg.executionContext)
}