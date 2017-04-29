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
package s_mach.concurrent.util

import java.util.concurrent.atomic.AtomicReference
import s_mach.concurrent.impl.AtomicFSMImpl

/**
 * A class for a finite state machine whose state may be transitioned
 * atomically by any number of concurrent threads.
 *
 * @tparam S the type of state
 */
trait AtomicFSM[I,S,O] {
  /** @return the current state */
  def current: S

  /**
    * Atomically transition the FSM
    * @return output from transitioning the FSM
    **/
  def apply(i: I) : O

  /**
    * Register a callback that is invoked whenever the FSM transitions
    * to a new state
    *
    * @param callback function called with old state as first arg and new
    *                 state as second whenever state is transitioned
    */
  def onTransition(callback: (S,S) => Unit) : Unit
}

object AtomicFSM {
  def apply[I,S,O](
    state: AtomicReference[S]
  )(
    transition: (S,I) => S
  )(
    afterTransition: (S,S) => O
  ) : AtomicFSM[I,S,O] = {
    new AtomicFSMImpl[I,S,O](state,transition,afterTransition)
  }
  def apply[I,S,O](
    initialState: S
  )(
    transition: (S,I) => S
  )(
    afterTransition: (S,S) => O
  ) : AtomicFSM[I,S,O] = {
    apply(new AtomicReference[S](initialState))(transition)(afterTransition)
  }
}

