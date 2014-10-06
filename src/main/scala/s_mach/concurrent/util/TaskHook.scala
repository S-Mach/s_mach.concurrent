package s_mach.concurrent.util

import scala.language.higherKinds
import s_mach.concurrent.util.TaskHook.StepId

import scala.concurrent.{ExecutionContext, Future}

object TaskHook {
  type StepId = Int
}

trait TaskHook {
  def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext) : () => Future[R] = task
}

trait TaskStepHook {
  import TaskHook._

  def hookStep0[R](step: StepId => Future[R])(implicit ec:ExecutionContext) : StepId => Future[R]
  def hookStep1[A,R](step: (StepId, A) => Future[R])(implicit ec:ExecutionContext) : (StepId, A) => Future[R]
  def hookStep2[A,B,R](step: (StepId, A,B) => Future[R])(implicit ec:ExecutionContext) : (StepId,A,B) => Future[R]
}

trait TaskRunner extends TaskHook with TaskStepHook {
  def runTask0[A,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], () => Future[A]) => Future[ZZ],
    f: () => Future[A]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { () =>
        hookStep0({ (stepId:Int) => f() }).apply(stepIdGen.incrementAndGet())
      })
    }.apply()
  }

  def runTask1[A,B,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], A => Future[B]) => Future[ZZ],
    f: A => Future[B]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { a:A =>
        hookStep1({ (stepId:Int, a:A) => f(a) }).apply(stepIdGen.incrementAndGet(), a)
      })
    }.apply()
  }

  def runTask2[A,B,C,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], (A,B) => Future[C]) => Future[ZZ],
    f: (A,B) => Future[C]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { (a:A,b:B) =>
        hookStep2({ (stepId:Int, a:A, b:B) => f(a,b) }).apply(stepIdGen.incrementAndGet(), a, b)
      })
    }.apply()
  }

  def runTupleTask2[A,B](
    fa: () => Future[A],
    fb: () => Future[B],
    runner: (() => Future[A], () => Future[B]) => Future[(A,B)]
  )(implicit ec:ExecutionContext) : Future[(A,B)] = {
    val wfa = { () => hookStep0 { stepId:Int => fa() }.apply(1) }
    val wfb = { () => hookStep0 { stepId:Int => fb() }.apply(2) }
    hookTask { () => runner(wfa, wfb) }.apply()
  }
}

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