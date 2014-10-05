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
import s_mach.concurrent._

/**
 * A trait for reporting progress of a task that consists of one or more discrete steps. Each step is identified by an 
 * ordinal step identifier. The step id is a generic concept and may be mapped by callers to any concrete concept. For 
 * tasks with a known fixed input size, the step id is mapped directly to the index of the input (i.e. input with index 
 * 0 = step 0, index 1 = step 1, etc). For tasks with an unknown input size, the step id is mapped to the Nth item 
 * encountered minus one (i.e. 1st item = step 0, 2nd item = step 1). The step id must be incrementally assigned, 
 * ensuring that that total steps always equals last step id - 1.
 *
 * Note: the progress reporter is assumed to be stateful. All derived implementations must be thread safe
 */
trait TaskEventListener extends TaskHook {
  /** Called at the beginning of the computation */
  def onStartTask() : Unit
  /** Called once the computation completes */
  def onCompleteTask() : Unit
  /** Called at the beginning of execution of a step of the computation */
  def onStartStep(stepId: Long) : Unit
  /** Called at the beginning of execution of a step of the computation */
  def onCompleteStep(stepId: Long) : Unit
}

trait TaskEventListenerHook extends TaskEventListener with TaskHook with TaskStepHook {
  import TaskHook._

  override def hookTask[A](task: () => Future[A])(implicit ec:ExecutionContext) : () => Future[A] = {
    { () =>
      onStartTask()
      task() sideEffect onCompleteTask()
    }
  }
  override def hookStep0[R](step: StepId => Future[R])(implicit ec:ExecutionContext) : StepId => Future[R] = {
    { stepId =>
      onStartStep(stepId)
      step(stepId) sideEffect onCompleteStep(stepId)
    }
  }

  override def hookStep1[A,R](step: (StepId,A) => Future[R])(implicit ec:ExecutionContext) : (StepId,A) => Future[R] = {
    { (stepId,a) =>
      onStartStep(stepId)
      step(stepId,a) sideEffect onCompleteStep(stepId)
    }
  }

  override def hookStep2[A,B,R](step: (StepId,A,B) => Future[R])(implicit ec:ExecutionContext) : (StepId,A,B) => Future[R] = {
    { (stepId,a,b) =>
      onStartStep(stepId)
      step(stepId,a,b) sideEffect onCompleteStep(stepId)
    }
  }
}
