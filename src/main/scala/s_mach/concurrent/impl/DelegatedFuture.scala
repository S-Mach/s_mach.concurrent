package s_mach.concurrent.impl

import scala.concurrent.duration.Duration
import scala.concurrent.{TimeoutException, CanAwait, ExecutionContext, Future}
import scala.util.Try

trait DelegatedFuture[A] extends Future[A] {
  def delegate : Future[A]

  override final def onComplete[U](f: (Try[A]) => U)(implicit executor: ExecutionContext): Unit = delegate.onComplete(f)
  override final def isCompleted: Boolean = delegate.isCompleted
  override final def value: Option[Try[A]] = delegate.value
  @scala.throws[Exception](classOf[Exception])
  override final def result(atMost: Duration)(implicit permit: CanAwait): A = delegate.result(atMost)
  @scala.throws[InterruptedException](classOf[InterruptedException])
  @scala.throws[TimeoutException](classOf[TimeoutException])
  override final def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    delegate.ready(atMost)
    this
  }
}
