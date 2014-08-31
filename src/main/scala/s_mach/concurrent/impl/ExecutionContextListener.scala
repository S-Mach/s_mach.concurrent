/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____        __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.impl

import scala.concurrent.ExecutionContext

/**
 * An immutable ExecutionContext wrapper that allows listening to the ExecutionContext events of the delegate.
 */
class ExecutionContextListener(
  delegate: ExecutionContext,
  _onPrepare: NotifiableListener[Any, Unit] = NotifiableListener(),
  _onExec: NotifiableListener[Any, Runnable] = NotifiableListener(),
  _onStart: NotifiableListener[Any, Runnable] = NotifiableListener(),
  _onComplete: NotifiableListener[Any, Runnable] = NotifiableListener(),
  _onFail: NotifiableListener[Any, Throwable] = NotifiableListener()
) extends ExecutionContext {

  override def prepare() = {
    _onPrepare.notifyListeners(())
    val newDelegate = delegate.prepare()
    if(newDelegate eq delegate) {
      this
    } else {
      copy(delegate = newDelegate)
    }
  } 
  
  override def execute(runnable: Runnable) = {
    _onExec.notifyListeners(runnable)
    delegate.execute(new Runnable {
      override def run() {
        _onStart.notifyListeners(runnable)
        runnable.run()
        _onComplete.notifyListeners(runnable)
      }
    })
  }
  
  override def reportFailure(cause: Throwable) = {
    _onFail.notifyListeners(cause)
    delegate.reportFailure(cause)
  }

  def onPrepare = _onPrepare.lens(l => copy(_onPrepare = l))
  def onExec = _onExec.lens(l => copy(_onExec = l))
  def onStart = _onStart.lens(l => copy(_onStart = l))
  def onComplete = _onComplete.lens(l => copy(_onComplete = l))
  def onFail = _onFail.lens(l => copy(_onFail = l))

  protected def copy(
    delegate: ExecutionContext = delegate,
    _onPrepare: NotifiableListener[Any, Unit] = _onPrepare,
    _onExec: NotifiableListener[Any, Runnable] = _onExec,
    _onStart: NotifiableListener[Any, Runnable] = _onStart,
    _onComplete: NotifiableListener[Any, Runnable] = _onComplete,
    _onFail: NotifiableListener[Any, Throwable] = _onFail
  ) = new ExecutionContextListener(
    delegate = delegate,
    _onPrepare = _onPrepare,
    _onExec = _onExec,
    _onStart = _onStart,
    _onComplete = _onComplete,
    _onFail = _onFail
  )

}

object ExecutionContextListener {
  def apply(delegate: ExecutionContext) : ExecutionContextListener = new ExecutionContextListener(delegate)
}