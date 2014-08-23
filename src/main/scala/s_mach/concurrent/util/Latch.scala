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
package s_mach.concurrent.util

import scala.concurrent.{Promise, Future, ExecutionContext}
import s_mach.concurrent._

import scala.util.Try

/**
 * A trait for a latch that can be set once to synchronize events that are waiting on the barrier.
 */
trait Latch extends Barrier {
  /**
    * Set the latch
    * @throws IllegalArgumentException if latch has already been set
    * */
  def set() : Unit

  /**
   * Try to set the latch
   * @return TRUE if the latch is now set FALSE if the latch has already been set
   */
  def trySet() : Boolean
}

object Latch {
  val defaultFailMessage = "Latch is already set!"
  class LatchImpl(val failMessage: String) extends Latch {
    private[this] val promise = Promise[Unit]()
    val future = promise.future

    override def set() {
      if(promise.trySuccess(()) == false) {
        throw new IllegalStateException(failMessage)
      }
    }

    override def trySet() = promise.trySuccess(())

    override def isSet = promise.isCompleted

    override def onSet[A](f: () => A)(implicit ec: ExecutionContext) = {
      if(isSet) {
        Future.fromTry(Try(f()))
      } else {
        val promiseA = Promise[A]()
        future onSuccess { case _ => promiseA.complete(Try(f())) }
        promiseA.future
      }
    }
    override def happensBefore[A](next: => Future[A])(implicit ec:ExecutionContext) = future happensBefore next

  }

  def apply(failMessage: String = defaultFailMessage) : Latch = new LatchImpl(failMessage)
}