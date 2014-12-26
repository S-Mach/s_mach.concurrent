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
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.impl

//class LazyTuple2[T1,T2](__1: => T1, __2: => T2) extends Product2[T1,T2] {
//  override lazy val _1 = __1
//  override lazy val _2 = __2
//
//  override def toString = "(" + _1 + "," + _2 + ")"
//
//  def swap: Tuple2[T2,T1] = Tuple2(_2, _1)
//
//  def canEqual(other: Any): Boolean = other.isInstanceOf[LazyTuple2] || other.isInstanceOf[Tuple2]
//
//  override def equals(other: Any): Boolean = other match {
//    case that: Tuple2 =>
//      (that canEqual this) &&
//        _1 == that._1 &&
//        _2 == that._2
//    case that: LazyTuple2 =>
//      (that canEqual this) &&
//        _1 == that._1 &&
//        _2 == that._2
//    case _ => false
//  }
//
//  override def hashCode(): Int = {
//    val state = Seq(_1, _2)
//    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
//  }
//}
//
//object LazyTuple2 {
//  trait global {
//    @inline def lazyeval[T1,T2](t1: T1, t2: T2) = LazyTuple2(t1,t2)
//    @inline def ->[T1,T2](t1: T1, t2: T2) = LazyTuple2(t1,t2)
//  }
//  def apply[T1,T2](t1: => T1, t2: => T2): LazyTuple2[T1,T2] = {
//    new LazyTuple2(t1,t2)
//  }
//}


