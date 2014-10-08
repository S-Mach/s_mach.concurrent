package s_mach.concurrent.impl

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}

trait TaskHook {
  def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext) : () => Future[R]
}

trait TaskStepHook {
  def hookStep0[R](step: StepId => Future[R])(implicit ec:ExecutionContext) : StepId => Future[R]
  def hookStep1[A,R](step: (StepId, A) => Future[R])(implicit ec:ExecutionContext) : (StepId, A) => Future[R]
  def hookStep2[A,B,R](step: (StepId, A,B) => Future[R])(implicit ec:ExecutionContext) : (StepId,A,B) => Future[R]
}

