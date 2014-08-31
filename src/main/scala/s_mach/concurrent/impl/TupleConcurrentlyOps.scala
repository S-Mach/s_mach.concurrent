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

/* WARNING: Generated code. To modify see s_mach.concurrent.codegen.TupleConcurrentlyOpsCodeGen */

import s_mach.concurrent.impl.MergeOps.mergeFailImmediately

import scala.concurrent.{ExecutionContext, Future, Promise}

object TupleConcurrentlyOps extends TupleConcurrentlyOps
trait TupleConcurrentlyOps {
  
  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,ZZ](
    fa: Future[A],
    fb: Future[B]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B)] = {
    val promise = Promise[(A,B)]()
    mergeFailImmediately(promise, Vector(fa,fb))
    val future =
      for {
        a <- fa
        b <- fb
      } yield (a,b)

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C)] = {
    val promise = Promise[(A,B,C)]()
    mergeFailImmediately(promise, Vector(fa,fb,fc))
    val future =
      for {
        a <- fa
        b <- fb
        c <- fc
      } yield (a,b,c)

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D)] = {
    val promise = Promise[(A,B,C,D)]()
    mergeFailImmediately(promise, Vector(fa,fb,fc,fd))
    val future =
      for {
        a <- fa
        b <- fb
        c <- fc
        d <- fd
      } yield (a,b,c,d)

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E)] = {
    val promise = Promise[(A,B,C,D,E)]()
    mergeFailImmediately(promise, Vector(fa,fb,fc,fd,fe))
    val future =
      for {
        a <- fa
        b <- fb
        c <- fc
        d <- fd
        e <- fe
      } yield (a,b,c,d,e)

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F)] = {
    val promise = Promise[(A,B,C,D,E,F)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G)] = {
    val promise = Promise[(A,B,C,D,E,F,G)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q],
    fr: Future[R]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q],
    fr: Future[R],
    fs: Future[S]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q],
    fr: Future[R],
    fs: Future[S],
    ft: Future[T]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q],
    fr: Future[R],
    fs: Future[S],
    ft: Future[T],
    fu: Future[U]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }


  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,ZZ](
    fa: Future[A],
    fb: Future[B],
    fc: Future[C],
    fd: Future[D],
    fe: Future[E],
    ff: Future[F],
    fg: Future[G],
    fh: Future[H],
    fi: Future[I],
    fj: Future[J],
    fk: Future[K],
    fl: Future[L],
    fm: Future[M],
    fn: Future[N],
    fo: Future[O],
    fp: Future[P],
    fq: Future[Q],
    fr: Future[R],
    fs: Future[S],
    ft: Future[T],
    fu: Future[U],
    fv: Future[V]
  )(implicit
    ec:ExecutionContext
  ) : Future[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)] = {
    val promise = Promise[(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V)]()
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

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }

}

