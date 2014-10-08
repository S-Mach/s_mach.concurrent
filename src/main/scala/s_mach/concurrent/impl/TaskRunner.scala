package s_mach.concurrent.impl

import scala.language.higherKinds
import s_mach.concurrent.util.Semaphore
import scala.concurrent.{ExecutionContext, Future}

trait TaskRunner extends TaskHook with TaskStepHook {
  def runTask0[A,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], () => Future[A]) => Future[ZZ],
    f: () => Future[A]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { () =>
        hookStepFunction0({ (stepId:Int) => f() }).apply(stepIdGen.incrementAndGet())
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
        hookStepFunction1({ (stepId:Int, a:A) => f(a) }).apply(stepIdGen.incrementAndGet(), a)
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
        hookStepFunction2({ (stepId:Int, a:A, b:B) => f(a,b) }).apply(stepIdGen.incrementAndGet(), a, b)
      })
    }.apply()
  }

}

