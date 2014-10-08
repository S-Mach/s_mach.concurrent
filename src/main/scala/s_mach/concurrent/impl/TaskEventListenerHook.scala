package s_mach.concurrent.impl

import scala.concurrent.{ExecutionContext, Future}
import s_mach.concurrent.util.TaskEventListener
import s_mach.concurrent._

trait TaskEventListenerHook extends TaskEventListener with TaskHook with TaskStepHook {

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
