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

object AtomicStateImpl {
  private val areNotEqual : (Any,Any) => Boolean = _ != _
}

class AtomicStateImpl[S](
  initialState: S
) extends AtomicState[S] {
  import AtomicReferenceOps._
  import AtomicStateImpl._

  private[this] val _state = new AtomicReference[S](initialState)

  def current = _state.get

  private[this] val pickNewState : (S,S) => S = (_,newState) => newState

  def set(transition: S => S) =
    casLoopSet(_state,transition,pickNewState)

  def maybeSet(transition: PartialFunction[S, S]) =
    casLoopMaybeSet(_state,transition,areNotEqual)

  def setAndThen[X](
    transition: S => S
  )(
    afterTransition: (S,S)  => X
  ) =
    casLoopSet(_state, transition, afterTransition)

  def maybeSetAndThen[X](
    transition: PartialFunction[S, S]
  )(
    afterTransition: (S,S) => X
  ) =
    casLoopMaybeSet(_state, transition, afterTransition)

  def asFSM[I, X](transition: (S, I) => S)(afterTransition: (S, S) => X) =
    AtomicFSM(_state)(transition)(afterTransition)
}
