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

/**
 * A trait for adding or removing event listeners.
 * @tparam ID identifier type
 * @tparam E event type
 * @tparam MDT most derived type
 */
trait Listener[ID,E, MDT] {
  /** @return a copy of MDT with a new listener of id added */
  def add(id: ID)(f: (ID,E) => Unit) : MDT
  /** @return a copy of MDT with the listener specified by id removed */
  def remove(id: ID) : MDT
}

/**
 * An implementation of Listener used to add a listener as a member of the case class. Not intended to be exposed 
 * directly to callers. Instead the method lense should be exposed for modifying the inner value.
 * 
 * @param listeners current listeners
 * @tparam ID identifier type
 * @tparam E event type
 */
case class NotifiableListener[ID,E](listeners: List[(ID , (ID,E) => Unit)] = Nil) extends Listener[ID,E,NotifiableListener[ID,E]] { self =>
  
  /** Notify all listeners of an event */
  def notifyListeners(event: E) : Unit = listeners.foreach { case (a,f) => f(a,event) }
  
  /** @return a lense that allows callers to modify the list of listeners by copying the parent case class and updating
    *         the listeners */
  def lens[T](tCopy: NotifiableListener[ID,E] => T) : Listener[ID,E,T] = {
    new Listener[ID,E,T] {
      override def add(id: ID)(f: (ID, E) => Unit) = tCopy(self.add(id)(f))
      override def remove(id: ID) = tCopy(self.remove(id))
    }
  }
  
  def add(id: ID)(f: (ID,E) => Unit) = copy((id,f) :: listeners)
  
  def remove(id: ID) = {
    listeners.indexWhere(_._1 == id) match {
      case -1 => this
      case idx =>
        copy(listeners.take(idx) ::: listeners.drop(idx + 1))
    }
  }
}