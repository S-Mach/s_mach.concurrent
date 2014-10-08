package s_mach.concurrent.impl

import scala.concurrent.{ExecutionContext, Future}

trait DelegatingTaskRunner extends TaskRunner {
  def taskHooks : Seq[TaskHook]
  def taskStepHooks : Seq[TaskStepHook]

  override def hookStep0[R](step: (StepId) => Future[R])(implicit ec:ExecutionContext): (StepId) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStep0(step))
  }

  override def hookStep1[A, R](step: (StepId, A) => Future[R])(implicit ec:ExecutionContext): (StepId, A) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStep1(step))
  }

  override def hookStep2[A, B, R](step: (StepId, A, B) => Future[R])(implicit ec:ExecutionContext): (StepId, A, B) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStep2(step))
  }

  override def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext): () => Future[R] = {
    taskHooks.foldLeft(task)((task, taskHook) => taskHook.hookTask(task))
  }
}