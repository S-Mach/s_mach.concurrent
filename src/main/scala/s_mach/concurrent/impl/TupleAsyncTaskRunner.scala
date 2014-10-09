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

/* WARNING: Generated code. To modify see s_mach.concurrent.codegen.TupleAsyncTaskRunnerCodeGen */

import s_mach.concurrent.util.Semaphore
import scala.concurrent.{Promise, ExecutionContext, Future}
import s_mach.concurrent.impl.MergeOps.mergeFailImmediately

trait SMach_Concurrent_AbstractPimpMyAsyncConfig extends Any {
  def self: AsyncConfig

  
  def run[A,B](
    fa: => Future[A],
    fb: => Future[B]
  )(implicit ec:ExecutionContext) : Future[(A,B)] = {
    Tuple2AsyncTaskRunner(self).run(fa,fb)
  }


  def run[A,B,C](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C]
  )(implicit ec:ExecutionContext) : Future[(A,B,C)] = {
    Tuple3AsyncTaskRunner(self).run(fa,fb,fc)
  }


  def run[A,B,C,D](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D)] = {
    Tuple4AsyncTaskRunner(self).run(fa,fb,fc,fd)
  }


  def run[A,B,C,D,E](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E)] = {
    Tuple5AsyncTaskRunner(self).run(fa,fb,fc,fd,fe)
  }


  def run[A,B,C,D,E,F](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F)] = {
    Tuple6AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff)
  }


  def run[A,B,C,D,E,F,G](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G)] = {
    Tuple7AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg)
  }


  def run[A,B,C,D,E,F,G,H](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H)] = {
    Tuple8AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh)
  }


  def run[A,B,C,D,E,F,G,H,I](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I)] = {
    Tuple9AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi)
  }


  def run[A,B,C,D,E,F,G,H,I,J](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J)] = {
    Tuple10AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K)] = {
    Tuple11AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L)] = {
    Tuple12AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M)] = {
    Tuple13AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)] = {
    Tuple14AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)] = {
    Tuple15AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)] = {
    Tuple16AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)] = {
    Tuple17AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)] = {
    Tuple18AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)] = {
    Tuple19AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)] = {
    Tuple20AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T],
    fu: => Future[U]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)] = {
    Tuple21AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft,fu)
  }


  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T],
    fu: => Future[U],
    fv: => Future[V]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)] = {
    Tuple22AsyncTaskRunner(self).run(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft,fu,fv)
  }

}


case class Tuple2AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B](
    fa: => Future[A],
    fb: => Future[B]
  )(implicit ec:ExecutionContext) : Future[(A,B)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    hookTask { () =>
      val promise = Promise[(A,B)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      mergeFailImmediately(promise, Vector(fa,fb))
      val future =
        for {
          a <- fa
          b <- fb
        } yield (a,b)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple3AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C]
  )(implicit ec:ExecutionContext) : Future[(A,B,C)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    hookTask { () =>
      val promise = Promise[(A,B,C)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      mergeFailImmediately(promise, Vector(fa,fb,fc))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
        } yield (a,b,c)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple4AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
        } yield (a,b,c,d)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple5AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
        } yield (a,b,c,d,e)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple6AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
        } yield (a,b,c,d,e,f)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple7AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
        } yield (a,b,c,d,e,f,g)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple8AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
        } yield (a,b,c,d,e,f,g,h)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple9AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
        } yield (a,b,c,d,e,f,g,h,i)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple10AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
        } yield (a,b,c,d,e,f,g,h,i,j)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple11AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
        } yield (a,b,c,d,e,f,g,h,i,j,k)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple12AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
        } yield (a,b,c,d,e,f,g,h,i,j,k,l)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple13AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple14AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple15AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple16AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple17AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple18AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    val wfr = { () => hookStepFunction0 { stepId:Int => fr }.apply(18) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      val fr = semaphore.acquire(1)(wfr())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
          r <- fr
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple19AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    val wfr = { () => hookStepFunction0 { stepId:Int => fr }.apply(18) }
    val wfs = { () => hookStepFunction0 { stepId:Int => fs }.apply(19) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      val fr = semaphore.acquire(1)(wfr())
      val fs = semaphore.acquire(1)(wfs())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
          r <- fr
          s <- fs
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple20AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    val wfr = { () => hookStepFunction0 { stepId:Int => fr }.apply(18) }
    val wfs = { () => hookStepFunction0 { stepId:Int => fs }.apply(19) }
    val wft = { () => hookStepFunction0 { stepId:Int => ft }.apply(20) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      val fr = semaphore.acquire(1)(wfr())
      val fs = semaphore.acquire(1)(wfs())
      val ft = semaphore.acquire(1)(wft())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
          r <- fr
          s <- fs
          t <- ft
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple21AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T],
    fu: => Future[U]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    val wfr = { () => hookStepFunction0 { stepId:Int => fr }.apply(18) }
    val wfs = { () => hookStepFunction0 { stepId:Int => fs }.apply(19) }
    val wft = { () => hookStepFunction0 { stepId:Int => ft }.apply(20) }
    val wfu = { () => hookStepFunction0 { stepId:Int => fu }.apply(21) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      val fr = semaphore.acquire(1)(wfr())
      val fs = semaphore.acquire(1)(wfs())
      val ft = semaphore.acquire(1)(wft())
      val fu = semaphore.acquire(1)(wfu())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft,fu))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
          r <- fr
          s <- fs
          t <- ft
          u <- fu
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


case class Tuple22AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V](
    fa: => Future[A],
    fb: => Future[B],
    fc: => Future[C],
    fd: => Future[D],
    fe: => Future[E],
    ff: => Future[F],
    fg: => Future[G],
    fh: => Future[H],
    fi: => Future[I],
    fj: => Future[J],
    fk: => Future[K],
    fl: => Future[L],
    fm: => Future[M],
    fn: => Future[N],
    fo: => Future[O],
    fp: => Future[P],
    fq: => Future[Q],
    fr: => Future[R],
    fs: => Future[S],
    ft: => Future[T],
    fu: => Future[U],
    fv: => Future[V]
  )(implicit ec:ExecutionContext) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)] = {
    val wfa = { () => hookStepFunction0 { stepId:Int => fa }.apply(1) }
    val wfb = { () => hookStepFunction0 { stepId:Int => fb }.apply(2) }
    val wfc = { () => hookStepFunction0 { stepId:Int => fc }.apply(3) }
    val wfd = { () => hookStepFunction0 { stepId:Int => fd }.apply(4) }
    val wfe = { () => hookStepFunction0 { stepId:Int => fe }.apply(5) }
    val wff = { () => hookStepFunction0 { stepId:Int => ff }.apply(6) }
    val wfg = { () => hookStepFunction0 { stepId:Int => fg }.apply(7) }
    val wfh = { () => hookStepFunction0 { stepId:Int => fh }.apply(8) }
    val wfi = { () => hookStepFunction0 { stepId:Int => fi }.apply(9) }
    val wfj = { () => hookStepFunction0 { stepId:Int => fj }.apply(10) }
    val wfk = { () => hookStepFunction0 { stepId:Int => fk }.apply(11) }
    val wfl = { () => hookStepFunction0 { stepId:Int => fl }.apply(12) }
    val wfm = { () => hookStepFunction0 { stepId:Int => fm }.apply(13) }
    val wfn = { () => hookStepFunction0 { stepId:Int => fn }.apply(14) }
    val wfo = { () => hookStepFunction0 { stepId:Int => fo }.apply(15) }
    val wfp = { () => hookStepFunction0 { stepId:Int => fp }.apply(16) }
    val wfq = { () => hookStepFunction0 { stepId:Int => fq }.apply(17) }
    val wfr = { () => hookStepFunction0 { stepId:Int => fr }.apply(18) }
    val wfs = { () => hookStepFunction0 { stepId:Int => fs }.apply(19) }
    val wft = { () => hookStepFunction0 { stepId:Int => ft }.apply(20) }
    val wfu = { () => hookStepFunction0 { stepId:Int => fu }.apply(21) }
    val wfv = { () => hookStepFunction0 { stepId:Int => fv }.apply(22) }
    hookTask { () =>
      val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)]()
      val semaphore = Semaphore(workerCount)
      val fa = semaphore.acquire(1)(wfa())
      val fb = semaphore.acquire(1)(wfb())
      val fc = semaphore.acquire(1)(wfc())
      val fd = semaphore.acquire(1)(wfd())
      val fe = semaphore.acquire(1)(wfe())
      val ff = semaphore.acquire(1)(wff())
      val fg = semaphore.acquire(1)(wfg())
      val fh = semaphore.acquire(1)(wfh())
      val fi = semaphore.acquire(1)(wfi())
      val fj = semaphore.acquire(1)(wfj())
      val fk = semaphore.acquire(1)(wfk())
      val fl = semaphore.acquire(1)(wfl())
      val fm = semaphore.acquire(1)(wfm())
      val fn = semaphore.acquire(1)(wfn())
      val fo = semaphore.acquire(1)(wfo())
      val fp = semaphore.acquire(1)(wfp())
      val fq = semaphore.acquire(1)(wfq())
      val fr = semaphore.acquire(1)(wfr())
      val fs = semaphore.acquire(1)(wfs())
      val ft = semaphore.acquire(1)(wft())
      val fu = semaphore.acquire(1)(wfu())
      val fv = semaphore.acquire(1)(wfv())
      mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft,fu,fv))
      val future =
        for {
          a <- fa
          b <- fb
          c <- fc
          d <- fd
          e <- fe
          f <- ff
          g <- fg
          h <- fh
          i <- fi
          j <- fj
          k <- fk
          l <- fl
          m <- fm
          n <- fn
          o <- fo
          p <- fp
          q <- fq
          r <- fr
          s <- fs
          t <- ft
          u <- fu
          v <- fv
        } yield (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v)
      future onSuccess { case t => promise.success(t) }
      promise.future
    }.apply()
  }
}


