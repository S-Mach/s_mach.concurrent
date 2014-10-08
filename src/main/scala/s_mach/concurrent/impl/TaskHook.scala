package s_mach.concurrent.impl

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}

trait TaskHook {
  def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext) : () => Future[R]
}

trait TaskStepHook {
  def hookStepFunction0[R](step: TaskStepId => Future[R])(implicit ec:ExecutionContext) : TaskStepId => Future[R]
  def hookStepFunction1[A,R](step: (TaskStepId, A) => Future[R])(implicit ec:ExecutionContext) : (TaskStepId, A) => Future[R]
  def hookStepFunction2[A,B,R](step: (TaskStepId, A,B) => Future[R])(implicit ec:ExecutionContext) : (TaskStepId,A,B) => Future[R]
}

