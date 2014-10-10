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
package s_mach.concurrent.impl

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}

trait TaskRunner extends TaskHook with TaskStepHook {
  def runTask0[A,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], () => Future[A]) => Future[ZZ],
    f: () => Future[A]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { () =>
        hookStepFunction0 { (stepId:Int) =>
          f()
        }.apply(stepIdGen.incrementAndGet())
      })
    }.apply()
  }

  def runTask1[A,B,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], A => Future[B]) => Future[ZZ],
    f: A => Future[B]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { a:A =>
        hookStepFunction1 { (stepId:Int, a:A) =>
          f(a)
        }.apply(stepIdGen.incrementAndGet(), a)
      })
    }.apply()
  }

  def runTask2[A,B,C,M[AA] <: TraversableOnce[AA],ZZ](
    ma: M[A],
    runner: (M[A], (A,B) => Future[C]) => Future[ZZ],
    f: (A,B) => Future[C]
  )(implicit ec:ExecutionContext) : Future[ZZ] = {
    hookTask { () =>
      val stepIdGen = new java.util.concurrent.atomic.AtomicInteger(0)
      runner(ma, { (a:A,b:B) =>
        hookStepFunction2 { (stepId:Int, a:A, b:B) =>
          f(a,b)
        }.apply(stepIdGen.incrementAndGet(), a, b)
      })
    }.apply()
  }

}

