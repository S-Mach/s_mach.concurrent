package s_mach.concurrent

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenTraversableOnce, mutable}
import scala.concurrent.Future
import scala.reflect.ClassTag

trait AsyncStream[+A] {
  def ++[A1 >: A](other: AsyncStream[A1]) : AsyncStream[A1]
  def ++[A1 >: A](other: GenTraversableOnce[A1]) : AsyncStream[A1]
  def +:[A1 >: A](elem: A1) : AsyncStream[A1]
  def :+[A1 >: A](elem: A1) : AsyncStream[A1]
  def collect[B](pf: PartialFunction[A,B]) : AsyncStream[B]
  def collectFirst[B](pf: PartialFunction[A,B]) : LazyFuture[Option[B]]
  def count(p: A => Boolean) : LazyFuture[Int]
  def drop(n: Int) : AsyncStream[A]
  def dropWhile(p: A => Boolean) : AsyncStream[A]
  def exists(p: A => Boolean) : LazyFuture[Boolean]
  def filter(p: A => Boolean) : AsyncStream[A]
  def filterNot(p: A => Boolean) : AsyncStream[A]
  def find(p: A => Boolean) : LazyFuture[Option[A]]
  def flatMap[B](f: A => AsyncStream[B]) : AsyncStream[B]
  def flatten[B](implicit ev: <:<[A,AsyncStream[B]]) : AsyncStream[B]
  def fold[A1 >: A](z: A1)(op: (A1,A1) => A1) : LazyFuture[A1]
  def foldLeft[B](z: B)(op: (B,A) => B) : LazyFuture[B]
  def foldRight[B](z: B)(op: (A,B) => B) : LazyFuture[B]
  def forall(p: A => Boolean) : LazyFuture[Boolean]
  def foreach(f: A => Unit) : LazyFuture[Unit]
  def groupBy[K](f: A => K) : LazyFuture[Map[K,AsyncStream[A]]]
  def head : A
  def headOption : Option[A]
  def init : AsyncStream[A]
  def inits: AsyncStream[AsyncStream[A]]
  def isEmpty : Boolean
  def last : LazyFuture[A]
  def lastOption : LazyFuture[Option[A]]
  def map[B](f: A => B) : AsyncStream[B]
  def max : LazyFuture[A]
  def maxBy[B](f: A => B) : LazyFuture[A]
  def min : LazyFuture[A]
  def minBy[B](f: A => B) : LazyFuture[B]
  def mkString : LazyFuture[String]
  def mkString(sep: String) : LazyFuture[String]
  def mkString(start: String, sep: String, end: String) : LazyFuture[String]
  def nonEmpty : Boolean
  def partition(p: A => Boolean) : (AsyncStream[A],AsyncStream[A])
  def reduce[A1 >: A](op: (A1,A1) => A1) : LazyFuture[A]
  def reduceLeft[B >: A](op: (B,A) => B) : LazyFuture[B]
  def reduceLeftOption[B >: A](op: (B,A) => B) : LazyFuture[Option[B]]
  def reduceOption[A1 >: A](op: (A1,A1) => A1) : LazyFuture[A1]
  def reduceRight[B >: A](op: (A,B) => B) : LazyFuture[B]
  def reduceRightOption[B >: A](op: (A,B) => B) : LazyFuture[Option[B]]
  def size : LazyFuture[Int]
  def slice(from: Int, until: Int) : AsyncStream[A]
  def span(p: A => Boolean) : (AsyncStream[A],AsyncStream[A])
  def splitAt(n: Int) : (AsyncStream[A], AsyncStream[A])
  def sum : LazyFuture[A]
  def tail : AsyncStream[A]
  def tails : AsyncStream[AsyncStream[A]]
  def take(n: Int) : AsyncStream[A]
  def takeWhile(p: A => Boolean) : AsyncStream[A]
  def to[A1 >: A,Col[_]](implicit cbf: CanBuildFrom[Nothing, A1, Col[A1]]): LazyFuture[Col[A1]]
  def toArray[A1 >: A](implicit c:ClassTag[A1]) : LazyFuture[Array[A1]]
  def toBuffer[A1 >: A] : LazyFuture[mutable.Buffer[A1]]
  def toIndexedSeq[A1 >: A] : LazyFuture[IndexedSeq[A1]]
  def toIterable[A1 >: A] : LazyFuture[Iterable[A1]]
  def toIterator[A1 >: A] : LazyFuture[Iterator[A1]]
  def toList[A1 >: A] : LazyFuture[List[A1]]
  def toMap[T,U](implicit ev: <:<[A,(T,U)]) : LazyFuture[Map[T,U]]
  def toSeq[A1 >: A] : LazyFuture[Seq[A1]]
  def toSet[A1 >: A] : LazyFuture[Set[A1]]
  def toStream[A1 >: A] : LazyFuture[Stream[A1]]
  def toTraversable[A1 >: A] : LazyFuture[Traversable[A1]]
  def toVector[A1 >: A] : LazyFuture[Vector[A1]]
  def unzip[T,U](implicit ev: <:<[A,(T,U)]) : (AsyncStream[T],AsyncStream[U])
  def unzip3[T,U,V](implicit ev: <:<[A,(T,U,V)]) : (AsyncStream[T],AsyncStream[U],AsyncStream[V])
  def withFilter(p: A => Boolean) : AsyncStream[A]

  trait Lifted {
    def ++[A1 >: A](other: AsyncStream[A1]) : AsyncStream[A1]
    def ++[A1 >: A](other: GenTraversableOnce[A1]) : AsyncStream[A1]
    def +:[A1 >: A](elem: A1) : AsyncStream[A1]
    def :+[A1 >: A](elem: A1) : AsyncStream[A1]
    def collect[B](pf: PartialFunction[A,B]) : AsyncStream[B]
    def collectFirst[B](pf: PartialFunction[A,B]) : LazyFuture[Option[B]]
    def count(p: A => Boolean) : LazyFuture[Int]
    def drop(n: Int) : AsyncStream[A]
    def dropWhile(p: A => Boolean) : AsyncStream[A]
    def exists(p: A => Boolean) : LazyFuture[Boolean]
    def filter(p: A => Boolean) : AsyncStream[A]
    def filterNot(p: A => Boolean) : AsyncStream[A]
    def find(p: A => Boolean) : LazyFuture[Option[A]]
    def flatMap[B](f: A => AsyncStream[B]) : AsyncStream[B]
    def flatten[B](implicit ev: <:<[A,AsyncStream[B]]) : AsyncStream[B]
    def fold[A1 >: A](z: A1)(op: (A1,A1) => A1) : LazyFuture[A1]
    def foldLeft[B](z: B)(op: (B,A) => B) : LazyFuture[B]
    def foldRight[B](z: B)(op: (A,B) => B) : LazyFuture[B]
    def forall(p: A => Boolean) : LazyFuture[Boolean]
    def foreach(f: A => Unit) : LazyFuture[Unit]
    def groupBy[K](f: A => K) : LazyFuture[Map[K,AsyncStream[A]]]
    def head : A
    def headOption : Option[A]
    def init : AsyncStream[A]
    def inits: AsyncStream[AsyncStream[A]]
    def isEmpty : Boolean
    def last : LazyFuture[A]
    def lastOption : LazyFuture[Option[A]]
    def map[B](f: A => Future[B]) : AsyncStream[B]#Lifted
    def max : LazyFuture[A]
    def maxBy[B](f: A => B) : LazyFuture[A]
    def min : LazyFuture[A]
    def minBy[B](f: A => B) : LazyFuture[B]
    def mkString : LazyFuture[String]
    def mkString(sep: String) : LazyFuture[String]
    def mkString(start: String, sep: String, end: String) : LazyFuture[String]
    def nonEmpty : Boolean
    def partition(p: A => Boolean) : (AsyncStream[A],AsyncStream[A])
    def reduce[A1 >: A](op: (A1,A1) => A1) : LazyFuture[A]
    def reduceLeft[B >: A](op: (B,A) => B) : LazyFuture[B]
    def reduceLeftOption[B >: A](op: (B,A) => B) : LazyFuture[Option[B]]
    def reduceOption[A1 >: A](op: (A1,A1) => A1) : LazyFuture[A1]
    def reduceRight[B >: A](op: (A,B) => B) : LazyFuture[B]
    def reduceRightOption[B >: A](op: (A,B) => B) : LazyFuture[Option[B]]
    def size : LazyFuture[Int]
    def slice(from: Int, until: Int) : AsyncStream[A]
    def span(p: A => Boolean) : (AsyncStream[A],AsyncStream[A])
    def splitAt(n: Int) : (AsyncStream[A], AsyncStream[A])
    def sum : LazyFuture[A]
    def tail : AsyncStream[A]
    def tails : AsyncStream[AsyncStream[A]]
    def take(n: Int) : AsyncStream[A]
    def takeWhile(p: A => Boolean) : AsyncStream[A]
    def to[A1 >: A,Col[_]](implicit cbf: CanBuildFrom[Nothing, A1, Col[A1]]): LazyFuture[Col[A1]]
    def toArray[A1 >: A](implicit c:ClassTag[A1]) : LazyFuture[Array[A1]]
    def toBuffer[A1 >: A] : LazyFuture[mutable.Buffer[A1]]
    def toIndexedSeq[A1 >: A] : LazyFuture[IndexedSeq[A1]]
    def toIterable[A1 >: A] : LazyFuture[Iterable[A1]]
    def toIterator[A1 >: A] : LazyFuture[Iterator[A1]]
    def toList[A1 >: A] : LazyFuture[List[A1]]
    def toMap[T,U](implicit ev: <:<[A,(T,U)]) : LazyFuture[Map[T,U]]
    def toSeq[A1 >: A] : LazyFuture[Seq[A1]]
    def toSet[A1 >: A] : LazyFuture[Set[A1]]
    def toStream[A1 >: A] : LazyFuture[Stream[A1]]
    def toTraversable[A1 >: A] : LazyFuture[Traversable[A1]]
    def toVector[A1 >: A] : LazyFuture[Vector[A1]]
    def unzip[T,U](implicit ev: <:<[A,(T,U)]) : (AsyncStream[T],AsyncStream[U])
    def unzip3[T,U,V](implicit ev: <:<[A,(T,U,V)]) : (AsyncStream[T],AsyncStream[U],AsyncStream[V])
    def withFilter(p: A => Boolean) : AsyncStream[A]
  }

  def lifted = new Lifted {

  }
}

object AsyncStream {
  def apply[A](values: A*) : AsyncStream[A]


}
