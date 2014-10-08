package s_mach.concurrent.impl

import scala.concurrent.{ExecutionContext, Future}

trait DelegatingTaskRunner extends TaskRunner {
  def taskHooks : Seq[TaskHook]
  def taskStepHooks : Seq[TaskStepHook]

  override def hookStepFunction0[R](step: (TaskStepId) => Future[R])(implicit ec:ExecutionContext): (TaskStepId) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction0(step))
  }

  override def hookStepFunction1[A, R](step: (TaskStepId, A) => Future[R])(implicit ec:ExecutionContext): (TaskStepId, A) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction1(step))
  }

  override def hookStepFunction2[A, B, R](step: (TaskStepId, A, B) => Future[R])(implicit ec:ExecutionContext): (TaskStepId, A, B) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction2(step))
  }

  override def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext): () => Future[R] = {
    taskHooks.foldLeft(task)((task, taskHook) => taskHook.hookTask(task))
  }
}