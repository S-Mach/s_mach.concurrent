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

import s_mach.concurrent.impl.AtomicStateImpl


/**
  * A class whose state may be transitioned atomically by any number of concurrent
  * threads.
  *
  * @tparam S the type of state
 */
trait AtomicState[S] {
  /** @return the current state */
  def current : S

  /**
    * Atomically transition the current state
    *
    * @param transition function to transition the state. May be called more than
    *                   once, should not have side effects
    * @return the new state
    */
  def set(transition: S => S) : S

  /**
    * Atomically transition current state and then invoke a code block exactly
    * once
    *
    * Note: all state transition side effects should be placed in the
    * afterTransition method
    *
    * @param transition function that accepts the current state and returns
    *                   the next state. This method may be called more than
    *                   once and should not have side effects.
    * @param afterTransition function called exactly once after successful CAS
    *                        that takes the old state as first arg and new state
    *                        as second.
    * @return the value returned by afterTransition
    */
  def setAndThen[X](
    transition: S => S
  )(
    afterTransition: (S,S) => X
  ) : X

  /**
    * Maybe atomically transition current state
    *
    * Note: all state transition side effects should be placed in the
    * afterTransition method
    *
    * @param transition partial function that accepts the current state and
    *                   returns the next state. This method may be called more than
    *                  once and should not have side effects.
    * @return TRUE if current state was successfully transitioned FALSE otherwise
   **/
  def maybeSet(transition: PartialFunction[S,S]) : Boolean

  /**
    * Maybe atomically transition current state and then invoke a block of code
    * exactly once
    *
    * Note: all state transition side effects should be placed in the
    * afterTransition method
    *
    * @param transition partial function that accepts the current state and
    *                   returns the next state. This method may be called more than
    *                  once and should not have side effects.
    * @param afterTransition function called exactly once after successful transition OR
    *                        after transition partial function fails to match. After
    *                        successful transition, passes old state as first arg and new
    *                        state as second. If no transition occurred then called with
    *                        current state for both first and second argument
    * @return the value returned by afterTransition
   **/
  def maybeSetAndThen[X](
    transition: PartialFunction[S,S]
  )(
    afterTransition: (S,S) => X
  ) : X

  /**
    * @return an AtomicFSM that shares the underlying state of this object
    */
  def asFSM[I,X](transition: (S,I) => S)(afterTransition: (S,S) => X) : AtomicFSM[I,S,X]
}

object AtomicState {
  def apply[S](initialState: S) : AtomicState[S] =
    new AtomicStateImpl[S](initialState)
}
