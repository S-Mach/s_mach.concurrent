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

import scala.annotation.tailrec

object AtomicReferenceOps {
  def casLoopSet[S,X](
    self: AtomicReference[S],
    transition: S => S,
    afterTransition: (S,S) => X
  ) : X = {
    @tailrec def loop() : X = {
      val oldState = self.get
      val newState = transition(oldState)
      if(self.compareAndSet(oldState, newState)) {
        afterTransition(oldState,newState)
      } else {
        loop()
      }
    }
    loop()
  }

  def casLoopMaybeSet[S,X](
    self: AtomicReference[S],
    transition: PartialFunction[S,S],
    afterTransition: (S,S) => X
  ) : X = {
    var matchError : Boolean = false
    @tailrec def loop() : X = {
      val oldState = self.get
      val newState = try {
        transition(oldState)
      } catch {
        case _:MatchError =>
          matchError = true
          oldState
      }
      if(matchError) {
        afterTransition(oldState,oldState)
      } else if(self.compareAndSet(oldState, newState)) {
        afterTransition(oldState, newState)
      } else {
        loop()
      }
    }
    loop()

// Note: original intended code but Scala 2.11 fails tailrec optimization
// for unknown reasons
//    @tailrec def loop() : X = {
//      val oldState = self.get
//      try {
//        val newState = transition(oldState)
//          if(self.compareAndSet(oldState, newState)) {
//            onTransition(oldState, newState)
//          } else {
//            loop()
//          }
//        } catch {
//          case _:MatchError =>
//            onTransition(oldState,oldState)
//        }
//    }
//    loop()
  }
  /**
    * Atomically transition the state of the FSM using CAS.
    *
    * Note: all state transition side effects should be placed in the
    * onTransition method
    *
    * @param input input to fold with state
    * @param transition function that accepts the current state and
    *                   input and returns the next state. This method may be
    *                  called more than once and should not have side effects.
    * @param afterTransition function called exactly once that takes the old state
    *                     as first arg and new state as second.
    * @return the value returned by afterTransition
   **/
  def casLoopFold[I,S,X](
    self: AtomicReference[S],
    input: I,
    transition: (S,I) => S,
    afterTransition: (S,S) => X
  ) : X = {
    @tailrec def loop() : X = {
      val oldState = self.get
      val newState = transition(oldState,input)
      if(self.compareAndSet(oldState, newState)) {
        afterTransition(oldState,newState)
      } else {
        loop()
      }
    }
    loop()
  }


}
