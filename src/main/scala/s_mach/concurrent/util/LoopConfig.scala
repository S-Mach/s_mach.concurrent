package s_mach.concurrent.util

import scala.concurrent.{ExecutionContext, Future}

trait LoopConfig {
  def onLoopStart() : Unit = { }
  def onLoopEnd() : Unit = { }

  @inline final def loop[A](f: => Future[A])(implicit ec:ExecutionContext) : Future[A] = {
    onLoopStart()
    val retv = f
    retv onComplete { case _ => onLoopEnd() }
    retv
  }
}
