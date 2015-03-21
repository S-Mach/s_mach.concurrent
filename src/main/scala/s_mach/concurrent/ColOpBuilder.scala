package s_mach.concurrent

import scala.collection.GenTraversableOnce
import scala.concurrent.Future

trait LiftedMonad[M[+_],Id[+_]] {
  def identity: Monad[Id]

  def bind[A](a: A) : Id[M[A]]
  def bind[A](a: GenTraversableOnce[A]) : Id[M[A]]
  def map[A,B](id:M[A])(f: A => Id[B]) : Id[M[B]]
  def flatMap[A,B](id:M[A])(f: A => Id[M[B]]) : Id[M[B]]
  def foreach[A,U](id:M[A])(f: A => Id[U]) : Id[Unit]
}

trait Monoid[M] {
  def empty : M
  def append(m1:M,m2:M) : M
}

trait Col[C[+_]] extends LiftedMonad[C,Id] with Monoid[C]

  trait ColOps[+A,C[+_],Id[+_]] {
    def col: Col[C]
    
    sealed trait Transform[+I,+O] extends (C[I] => Future[C[O]]) {
      type Input = I
      type Output = O
      def prev: Option[Transform[_,O]]
    }
  
    case class Append[+O](
      values: GenTraversableOnce[O],
      prev: Option[Transform[_,O]]
    ) extends Transform[O,O] {
      def apply[O1>:O](m:C[O1]) : Id[C[O1]] =
        col.identity.flatMap(col.bind(values)) { a =>

        }
    }
    abstract class Append1[+I](value: I) extends Transform[I,I]
    abstract class Collect[+I,O](pf: PartialFunction[I,O]) extends Transform[I,O]
    abstract class Drop[+I,O](n: Int) extends Transform[I,O]
    abstract class DropRight[+I,O](n: Int) extends Transform[I,O]
    abstract class DropWhile[+I,O](p: I => Boolean) extends Transform[I,O]
    abstract class Filter[+I,O](p: I => Boolean) extends Transform[I,O]
    abstract class Map[+I,O](f: I => O) extends Transform[I,O]
    abstract class FlatMap[+I,O](f: I => M[O]) extends Transform[I,O]
  
    sealed trait Reduction[+I,+O] extends (M[I] => O)
    abstract class CollectFirst[I,O](pf: PartialFunction[I,O]) extends Reduction[M[I],O]
    abstract class Count[I,O](pf: PartialFunction[I,O]) extends Reduction[I,O]
    abstract class Exists[I,O](pf: PartialFunction[I,O]) extends Reduction[I,O]
    abstract class Find[I,O](pf: PartialFunction[I,O]) extends Reduction[I,O]
    abstract class Fold[I](z: I, f: (I,I) => I) extends Reduction[I,I]
    abstract class FoldLeft[I,O](z: O, f: (O,I) => O) extends Reduction[I,O]
    abstract class FoldRight[I,O](z: O, f: (O,I) => O) extends Reduction[I,O]
  }
  
  trait CanRunOp[A,B,M[+_]] {
    def mkRunner : ColOpRunner[A,B,M]  
  }
  
  trait ColOpRunner[A,B,M[+_]] extends LazyFuture[B] {
    def xfrm: M[Nothing] => Future[M[A]]
    def op: M[A] => Future[B]
    def empty: M[Nothing]
  
    override def run() : Future[B] = for {
      xfrmResult <- xfrm(empty)
      opResult <- op(xfrmResult)
    } yield opResult
  }
  
  trait ColOpBuilder[+A,M[+_]] extends ColOps[M] {
    def xfrm: Nothing => M[A]
    
    protected def append[A1 >: A, B](xfrm: Transform[A1,B]) : ColOpBuilder[B,M]
  
    protected def apply[A1 >: A, B](op: Reduction[A1,B])(implicit canRunOp:CanRunOp[A1,B,M]) : ColOpRunner[A1,B,M]
    
  }  
}

