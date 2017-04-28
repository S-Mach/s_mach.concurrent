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
package s_mach.concurrent.impl

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.util.TaskEventListener
import s_mach.concurrent._

trait TaskEventListenerHook extends
  TaskEventListener with
  TaskHook with
  TaskStepHook {

  override def hookTask[A](
    task: () => Future[A]
  )(implicit ec:ExecutionContext) : () => Future[A] = {
    { () =>
      onStartTask()
      task() sideEffect onCompleteTask()
    }
  }
  override def hookStepFunction0[R](
    step: TaskStepId => Future[R]
  )(implicit ec:ExecutionContext) : TaskStepId => Future[R] = {
    { stepId =>
      onStartStep(stepId)
      step(stepId) sideEffect onCompleteStep(stepId)
    }
  }

  override def hookStepFunction1[A,R](
    step: (TaskStepId,A) => Future[R]
  )(implicit ec:ExecutionContext) : (TaskStepId,A) => Future[R] = {
    { (stepId,a) =>
      onStartStep(stepId)
      step(stepId,a) sideEffect onCompleteStep(stepId)
    }
  }

  override def hookStepFunction2[A,B,R](
    step: (TaskStepId,A,B) => Future[R]
  )(implicit ec:ExecutionContext) : (TaskStepId,A,B) => Future[R] = {
    { (stepId,a,b) =>
      onStartStep(stepId)
      step(stepId,a,b) sideEffect onCompleteStep(stepId)
    }
  }
}
