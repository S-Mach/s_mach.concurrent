package s_mach.concurrent.impl

import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import s_mach.concurrent._

case object Nil extends AsyncStream[Nothing] {
  def ++[A1 >: Nothing](other: AsyncStream[A1]) : AsyncStream[A1] =
    other

  def ++[A1 >: Nothing](other: GenTraversableOnce[A1]) : AsyncStream[A1] =
    ConsN[A1](other.toArray, this, _ => this.future)

  def +:[A1 >: Nothing](elem: A1) : AsyncStream[A1] =
    Cons(elem, this, _ => this.future)

  def :+[A1 >: Nothing](elem: A1) : AsyncStream[A1] =
    +:(elem)

  def collect[B](pf: PartialFunction[Nothing,B]) : AsyncStream[B] =
    this

  private[this] val none = None.future
  def collectFirst[B](pf: PartialFunction[Nothing,B]) : LazyFuture[Option[B]] =
    LazyFuture(none)

  private[this] val zero = 0.future
  def count(p: Nothing => Boolean) : LazyFuture[Int] =
    LazyFuture(zero)

  def drop(n: Int) : AsyncStream[Nothing] =
    this

  def dropWhile(p: Nothing => Boolean) : AsyncStream[Nothing] =
    this

  private[this] val _false = false.future
  def exists(p: Nothing => Boolean) : LazyFuture[Boolean] =
    LazyFuture(_false)

  def filter(p: Nothing => Boolean) : AsyncStream[Nothing] =
    this

  def filterNot(p: Nothing => Boolean) : AsyncStream[Nothing] =
    this

  def find(p: Nothing => Boolean) : LazyFuture[Option[Nothing]] =
    LazyFuture(none)

  def flatMap[B](f: Nothing => AsyncStream[B]) : AsyncStream[B] =
    this

  def flatten[B](implicit ev: <:<[Nothing,AsyncStream[B]]) : AsyncStream[B] =
    this

  def fold[A1 >: Nothing](z: A1)(op: (A1,A1) => A1) : LazyFuture[A1] =
    LazyFuture(z.future)

  def foldLeft[B](z: B)(op: (B,Nothing) => B) : LazyFuture[B] =
    LazyFuture(z.future)

  def foldRight[B](z: B)(op: (Nothing,B) => B) : LazyFuture[B] =
    LazyFuture(z.future)

  private[this] val _true = true.future
  def forall(p: Nothing => Boolean) : LazyFuture[Boolean] =
    LazyFuture(_true)

  def foreach(f: Nothing => Unit) : LazyFuture[Unit] =
    LazyFuture(Future.unit)

  def groupBy[K](f: Nothing => K) : LazyFuture[Map[K,AsyncStream[Nothing]]] =
    LazyFuture(Map.empty[K,AsyncStream[Nothing]].future)

  def head : Nothing =
    throw new NoSuchElementException

  def headOption : Option[Nothing] =
    None

  def init : AsyncStream[Nothing] =
    this

  def inits: AsyncStream[AsyncStream[Nothing]] =
    this

  def isEmpty : Boolean =
    true

  private[this] val noSuchElement : Future[Nothing] = Future.successful {
    throw new NoSuchElementException
  }

  def last : LazyFuture[Nothing] =
    LazyFuture(noSuchElement)

  def lastOption : LazyFuture[Option[Nothing]] =
    LazyFuture(none)

  def map[B](f: Nothing => B) : AsyncStream[B] =
    this

  def max : LazyFuture[Nothing] =
    throw new NoSuchElementException

  def maxBy[B](f: Nothing => B) : LazyFuture[Nothing] =
    throw new NoSuchElementException

  def min : LazyFuture[Nothing] =
    throw new NoSuchElementException

  def minBy[B](f: Nothing => B) : LazyFuture[B] =
    throw new NoSuchElementException

  private[this] val emptyString = "".future
  def mkString : LazyFuture[String] =
    LazyFuture(emptyString)

  def mkString(sep: String) : LazyFuture[String] =
    LazyFuture(emptyString)

  def mkString(start: String, sep: String, end: String) : LazyFuture[String] =
    LazyFuture(emptyString)

  def nonEmpty : Boolean =
    false

  def partition(p: Nothing => Boolean) : (AsyncStream[Nothing],AsyncStream[Nothing]) =
    (this, this)

  def reduce[A1 >: Nothing](op: (A1,A1) => A1) : LazyFuture[Nothing] =
    LazyFuture({ throw new NoSuchElementException}.future)
  def reduceLeft[B >: Nothing](op: (B,Nothing) => B) : LazyFuture[B] = ???
  def reduceLeftOption[B >: Nothing](op: (B,Nothing) => B) : LazyFuture[Option[B]] = ???
  def reduceOption[A1 >: Nothing](op: (A1,A1) => A1) : LazyFuture[A1] = ???
  def reduceRight[B >: Nothing](op: (Nothing,B) => B) : LazyFuture[B] = ???
  def reduceRightOption[B >: Nothing](op: (Nothing,B) => B) : LazyFuture[Option[B]] = ???
  def size : LazyFuture[Int] = ???
  def slice(from: Int, until: Int) : AsyncStream[Nothing] = ???
  def span(p: Nothing => Boolean) : (AsyncStream[Nothing],AsyncStream[Nothing]) = ???
  def splitAt(n: Int) : (AsyncStream[Nothing], AsyncStream[Nothing]) = ???
  def sum : LazyFuture[Nothing] = ???
  def tail : AsyncStream[Nothing] = ???
  def tails : AsyncStream[AsyncStream[Nothing]] = ???
  def take(n: Int) : AsyncStream[Nothing] = ???
  def takeWhile(p: Nothing => Boolean) : AsyncStream[Nothing] = ???
  def to[A1 >: Nothing,Col[_]](implicit cbf: CanBuildFrom[Nothing, A1, Col[A1]]): LazyFuture[Col[A1]] = ???
  def toArray[A1 >: Nothing](implicit c:ClassTag[A1]) : LazyFuture[Array[A1]] = ???
  def toBuffer[A1 >: Nothing] : LazyFuture[mutable.Buffer[A1]] = ???
  def toIndexedSeq[A1 >: Nothing] : LazyFuture[IndexedSeq[A1]] = ???
  def toIterable[A1 >: Nothing] : LazyFuture[Iterable[A1]] = ???
  def toIterator[A1 >: Nothing] : LazyFuture[Iterator[A1]] = ???
  def toList[A1 >: Nothing] : LazyFuture[List[A1]] = ???
  def toMap[T,U](implicit ev: <:<[Nothing,(T,U)]) : LazyFuture[Map[T,U]] = ???
  def toSeq[A1 >: Nothing] : LazyFuture[Seq[A1]] = ???
  def toSet[A1 >: Nothing] : LazyFuture[Set[A1]] = ???
  def toStream[A1 >: Nothing] : LazyFuture[Stream[A1]] = ???
  def toTraversable[A1 >: Nothing] : LazyFuture[Traversable[A1]] = ???
  def toVector[A1 >: Nothing] : LazyFuture[Vector[A1]] = ???
  def unzip[T,U](implicit ev: <:<[Nothing,(T,U)]) : (AsyncStream[T],AsyncStream[U]) = ???
  def unzip3[T,U,V](implicit ev: <:<[Nothing,(T,U,V)]) : (AsyncStream[T],AsyncStream[U],AsyncStream[V]) = ???
  def withFilter(p: Nothing => Boolean) : AsyncStream[Nothing] = ???

}

case class Cons[+A](
  value: A,
  prev: AsyncStream[A],
  fetchMore: Int => Future[AsyncStream[A]]
) extends AsyncStream[A]

case class ConsN[+A](
  values: Seq[A],
  prev: AsyncStream[A],
  fetchMore: Int => Future[AsyncStream[A]]
) extends AsyncStream[A]
