package s_mach.concurrent.impl

import s_mach.concurrent.util.Semaphore

import scala.concurrent.{ExecutionContext, Future}

case class Tuple2AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B](
    fa: => Future[A],
    fb: => Future[B]
  )(implicit ec:ExecutionContext) : Future[(A,B)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    hookTask { () =>
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      for {
        a <- fa
        b <- fb
      } yield (a,b)
    }.apply()
  }
}
