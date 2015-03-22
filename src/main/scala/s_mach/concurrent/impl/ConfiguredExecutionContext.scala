package s_mach.concurrent.impl

import java.util.concurrent.atomic.AtomicInteger
import s_mach.concurrent.util.{Semaphore, Throttler}

import scala.concurrent.{Future, ExecutionContext}
import s_mach.concurrent.config.AsyncConfig
import s_mach.concurrent._

trait ConfiguredExecutionContext extends ExecutionContext {
  def config: AsyncConfig
}

object ConfiguredExecutionContext {
  def apply(config: AsyncConfig, base: ExecutionContext) : ExecutionContext = {
    val _config = config

    // Use the base ExecutionContext for all Futures created below
    implicit val ec = base

    val semaphore = Semaphore(config.workerCount)

    val onStartProgress : Int => Unit = 
      config.optProgress match {
        case Some(progressConfig) =>
          import progressConfig._
          { stepId: Int =>
            if(stepId == 1) {
              reporter.onStartTask()  
            }
            reporter.onStartStep(stepId)
          }
        case None => { _ => }
      }
    
    val onEndProgress : Int => Unit =
      config.optProgress match {
        case Some(progressConfig) =>
          import progressConfig._
          { stepId: Int =>
            reporter.onCompleteStep(stepId)
            if(config.optTotal.contains(stepId)) {
              reporter.onCompleteTask()
            }
          }
        case None => { _ => }
      }
    
    val onFail : (Int,Throwable,() => Future[Unit]) => Future[Unit] =
      config.optRetry match {
        case Some(retryConfig) =>
          import retryConfig._
          { (stepId:Int,failure:Exception,repeat: () => Future[Unit]) =>
            for {
              retry <- retryer.shouldRetry(stepId,failure)
              _ <- if(retry) {
                repeat()
              } else {
                Future.failed(failure)
              }
            } yield ()
          }
        case None => { (_,_,_) => Future.unit }
      }

    val maybeThrottle : (() => Future[Unit]) => Future[Unit] =
      config.optThrottle match {
        case Some(throttleConfig) =>
          import throttleConfig._
          val throttler = Throttler(throttle_ns)

          { (loop:() => Future[Unit]) =>
             throttler.run(loop())
          }
        case None =>
        { (loop:() => Future[Unit]) => loop() }
      }

    new ConfiguredExecutionContext {
      val stepId = new AtomicInteger(0)
      
      override def config: AsyncConfig = _config

      override def reportFailure(cause: Throwable) = base.reportFailure(cause)

      override def execute(runnable: Runnable) = {
        base.execute(new Runnable {
          override def run(): Unit = {
            val nextStepId = stepId.incrementAndGet()
            onStartProgress(nextStepId)
            def loop() : Future[Unit] =
              Future(runnable.run()).recoverWith { case failure =>
                onFail(nextStepId,failure,loop)
              }
            val future = semaphore.acquire(1)(maybeThrottle(loop))
            future onComplete { case _ =>
              onEndProgress(nextStepId)
            }
            future.background
          }
        })  
      }
    }
    
  }
}
