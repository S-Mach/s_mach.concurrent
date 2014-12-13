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

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

/**
 * A class for a finite state machine whose state may be transitioned
 * atomically. During the time it takes to compute a new state for the FSM,
 * another thread may change the FSMs state. AtomicFSM is based on
 * AtomicReference. After computing a new state, AtomicReference CAS is used to
 * compares the current state to the one originally used to compute the new
 * state. If the current state does not match the original state used to compute
 * the new state, then the new state is discarded and the new state computation
 * is started again using the updated current state. This continues infinitely
 * until a new state successfully changes the current state. Because states may
 * be discarded and transition functions invoked multiple times during this
 * process, states and transition functions should never create side effects
 * themselves. Instead all transition side effects should be placed in the
 * onTransition method. When a state is successfully transitioned, onTransition
 * is called with both the original and new state. This method can be used to
 * safely create state transition side effects.
 *
 * @param s0 the initial state
 * @tparam S the type of state
 */
class AtomicFSM[S](s0: S) extends AtomicReference[S](s0) {

  /**
   * Atomically transition the state of the FSM. After successfully
   * transitioning to a new state, calls the onTransition method with the
   * previous state and the new state exactly once.
   * Note: all state transition side effects should be placed in the
   * onTransition method
   * @param transition partial function that accepts the current state and
   *                   returns the next state
   * @throws IllegalArgumentException if the transition function is undefined
   *                                  for some state
   * @return the new state
   */
  def apply(transition: PartialFunction[S,S]) : S = {
    @tailrec def loop() : S = {
      val oldState = get
      if(transition.isDefinedAt(oldState)) {
        val newState = transition(oldState)
        if(compareAndSet(oldState, newState)) {
          onTransition.orElse[(S,S),Unit] {
            case (before,after) =>
              throw new IllegalStateException(
                s"Illegal state transition from $before to $after"
              )
          }.apply((oldState, newState))
          newState
        } else {
          loop()
        }
      } else {
        throw new IllegalStateException(
          s"No transition for $oldState is defined"
        )
      }
    }
    loop()
  }

  /**
   * Atomically transition the state of the FSM and return the value associated
   * with that new state. After successfully transitioning to a new state, calls
   * the onTransition method with the previous state and the new state exactly
   * once.
   * Note: all state transition side effects should be placed in the
   * onTransition method
   * @param transition partial function that accepts the current state and
   *                   returns the next state and the return value
   * @throws IllegalArgumentException if the transition function is undefined
   *                                  for some state
   * @return the value associated with the new state
   */
  def fold[X](transition: PartialFunction[S,(S,X)]) : X = {
    @tailrec def loop() : X = {
      val oldState = get
      if(transition.isDefinedAt(oldState)) {
        val (newState,x) = transition(oldState)
        if(compareAndSet(oldState, newState)) {
          onTransition.orElse[(S,S),Unit] {
            case (before,after) =>
              throw new IllegalStateException(
                s"Illegal state transition from $before to $after"
              )
          }.apply((oldState, newState))
          x
        } else {
          loop()
        }
      } else {
        throw new IllegalStateException(
          s"No transition for $oldState is defined"
        )
      }
    }
    loop()
  }

  /**
   * Called exactly once with the old state and the new state when a transition
   * between states is successful. Override this method to safely create side
   * effects when state transitions occur.
   */
  def onTransition : PartialFunction[(S,S),Unit] = { case _ => }
}
