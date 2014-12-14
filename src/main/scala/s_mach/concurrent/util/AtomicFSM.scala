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
 * atomically by any number of concurrent threads.
 *
 * During the time it takes to compute a new state for the FSM, another thread
 * may change the FSMs state, invalidating the newly computed state. To deal
 * with this issue, AtomicFSM stores the state of the FSM using AtomicReference
 * and after computing a new state, uses AtomicReference CAS to atomically
 * compare the current state to the one originally used to compute the new
 * state. If the current state does not match the original state used to compute
 * the new state, then CAS update fails and the new state is discarded. The
 * whole process is repeated using the updated current state. This will repeat
 * until a new computed state successfully changes the current state. Because
 * states may be discarded and transition functions invoked multiple times
 * during this process, states and transition functions should never create
 * side effects themselves. Instead all transition side effects should be placed
 * in the onTransition method. When a state is successfully transitioned,
 * onTransition is called with both the original and new state. This method can
 * be used to safely create state transition side effects.
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
   * @param onTransition partial function that invokes side effects after a
   *                     successful state transition
   * @throws IllegalArgumentException if the transition function is undefined
   *                                  for some state
   * @return the new state
   */
  def apply(
    transition: PartialFunction[S,S],
    onTransition: PartialFunction[(S,S),Unit] = PartialFunction.empty
  ) : S = {
    @tailrec def loop() : S = {
      val oldState = get
      val newState = transition(oldState)
      if(compareAndSet(oldState, newState)) {
        val tuple = (oldState, newState)
        if(onTransition.isDefinedAt(tuple)) {
          onTransition(tuple)
        }
        newState
      } else {
        loop()
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
   * @param onTransition partial function that invokes side effects after a
   *                     successful state transition
   * @throws IllegalArgumentException if the transition function is undefined
   *                                  for some state
   * @return the value associated with the new state
   */
  def fold[X](
    transition: PartialFunction[S,(S,X)],
    onTransition: PartialFunction[(S,S),Unit] = PartialFunction.empty
  ) : X = {
    @tailrec def loop() : X = {
      val oldState = get
      val (newState,x) = transition(oldState)
      if(compareAndSet(oldState, newState)) {
        val tuple = (oldState, newState)
        if(onTransition.isDefinedAt(tuple)) {
          onTransition(tuple)
        }
        x
      } else {
        loop()
      }
    }
    loop()
  }

}
