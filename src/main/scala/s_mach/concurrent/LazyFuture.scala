package s_mach.concurrent

import s_mach.concurrent.util.DelegatedFuture
import scala.concurrent.{Promise, Future}

trait LazyFuture[+A] extends DeferredFuture[A] {
  def run() : Future[A]
}

object LazyFuture {
  case class LazyFutureImpl[+A](
    future: => Future[A]
  ) extends LazyFuture[A] with DelegatedFuture[A] {
    private[this] val _delegate = Promise[A]()
    override val delegate: Future[A] = _delegate.future

    private[this] val _deferred = Promise[Future[A]]()
    override val deferred: Future[Future[A]] = _deferred.future

    private[this] lazy val lazyFuture = {
      val startedFuture = future
      _delegate.completeWith(startedFuture)
      _deferred.success(delegate)
      startedFuture
    }
    override def run() = lazyFuture
  }

  def apply[A](future: => Future[A]) : LazyFuture[A] =
    LazyFutureImpl(future)
}
