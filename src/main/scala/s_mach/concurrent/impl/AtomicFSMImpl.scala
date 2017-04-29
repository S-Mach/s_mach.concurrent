/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____       __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2017 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.impl

import java.util.concurrent.atomic.AtomicReference

import s_mach.concurrent.util.{AtomicFSM, AtomicState}
import s_mach.codetools._

class AtomicFSMImpl[I,S,O](
  _state: AtomicReference[S],
  transition: (S,I) => S,
  afterTransition: (S,S) => O
) extends AtomicFSM[I,S,O] { self =>
  private[this] val callbacks = AtomicState[List[(S,S) => Unit]](Nil)
  private[this] val _doOnTransition = { (oldState:S, newState:S) =>
    val retv = afterTransition(oldState,newState)
    if(oldState != newState) {
      callbacks.current.foreach(_(oldState,newState))
    }
    retv
  }

  def current = _state.get

  def apply(i: I) =
    AtomicReferenceOps.casLoopFold[I,S,O](_state,i,transition,_doOnTransition)

  def onTransition(f: (S, S) => Unit) =
    callbacks.set(f :: _).discard
}

