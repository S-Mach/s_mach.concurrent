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
package s_mach.concurrent

import java.util.concurrent.Executors

import org.scalatest.{Tag, Matchers}
import s_mach.concurrent.util.{ConcurrentTestContext, SerializationSchedule}
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
import TestBuilder._

object ConcurrentTestCommon {
  // Not going to worry about shutdown since only one is ever created here
  implicit val scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors())
}

trait ConcurrentTestCommon extends Matchers {
  // Note: these tests will fail unless there is at least two cores
  assert(Runtime.getRuntime.availableProcessors() > 1)
  println(s"Found ${Runtime.getRuntime.availableProcessors()} CPUs")

  object DelayAccuracyTest extends Tag("s_mach.concurrent.DelayAccuracyTest")

  // Not going to worry about shutdown since only one is ever created here
  val cpus = Runtime.getRuntime.availableProcessors
  // Note: using more worker threads than cpus to increase inter-leavings and possible serialization schedules
  val executionContext = scala.concurrent.ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cpus * 2))
  val scheduledExecutionContext = ScheduledExecutionContext(cpus / 2)(executionContext)

  def mkConcurrentTestContext() = ConcurrentTestContext()(executionContext, scheduledExecutionContext)

  val DELAY = 100.micros
  val DELAY_NS = DELAY.toNanos
  val RANDOM_NS = (DELAY_NS * 0.1).toLong

  val ITEM_COUNT = 6
  val items = (1 to ITEM_COUNT).toVector

  val TEST_COUNT = 10000

  // Fuzz delays to generate more random serialization schedules
  def calcDelay_ns() =  DELAY_NS + (RANDOM_NS * Random.nextGaussian()).toLong

  def success(i: Int)(implicit ctc: ConcurrentTestContext) = {
    import ctc._

    sched.addStartEvent(s"success-$i")
    Future {
      delay(DELAY_NS)
      sched.addEndEvent(s"success-$i")
      i
    }
  }

  def successN(i: Int)(implicit ctc: ConcurrentTestContext) = {
    import ctc._

    sched.addStartEvent(s"success-$i")
    Future {
      delay(DELAY_NS)
      sched.addEndEvent(s"success-$i")
      Vector(i,i,i)
    }
  }

  def fail(i: Int)(implicit ctc: ConcurrentTestContext) = {
    import ctc._
    sched.addStartEvent(s"fail-$i")
    Future {
      sched.addEndEvent(s"fail-$i")
      throw new RuntimeException(s"fail-$i")
    }
  }

  def isConcurrentSchedule(itemCount: Int, sched: SerializationSchedule[String]) : Boolean = {

    val test1 =
      (1 to itemCount).forall { i =>
        sched.happensBefore("start",s"success-$i") &&
        sched.happensBefore(s"success-$i","end")
      }

    // Ensure some degree of concurrency
    val test2 =
      (1 to itemCount)
        .combinations(2)
        .map(v => (v(0),v(1)))
        .filter { case (i,j) => i != j }
        .exists { case (i,j) =>
          sched.happensDuring(s"success-$i",s"success-$j")
        }

    test1 && test2
  }

  def isSerialSchedule(itemCount: Int, sched: SerializationSchedule[String]) : Boolean = {
    val test1 =
      // Ensure serial execution
      sched.happensBefore("start","success-1")

    val test2 =
      (2 to (itemCount - 1)).forall { i =>
        sched.happensBefore(s"success-$i",s"success-${i+1}")
      }

    val test3 =
      sched.happensBefore(s"success-$itemCount","end")

    val test4 =
      // Ensure no concurrent execution
      (1 to itemCount)
        .combinations(2)
        .map(v => (v(0),v(1)))
        .filter { case (i,j) => i != j }
        .exists { case (i,j) =>
          sched.happensDuring(s"success-$i",s"success-$j")
        }

    test1 && test2 && test3 && !test4
  }

  def mean(values: TraversableOnce[Double]) = values.sum / values.size
  def stddev(mean: Double, values: TraversableOnce[Double]) = Math.sqrt(values.map(v => (mean - v) * (mean - v)).sum / values.size)
  // Not the best method, but for now throw away outliers based on greater than 2 stddevs from mean
  // Better solution: http://www.itl.nist.gov/div898/handbook/eda/section3/eda35h2.htm
  def isOutlier(mean: Double, stddev: Double)(v: Double) : Boolean = Math.abs(v - mean) > stddev * 2.0

  import scala.language.higherKinds
  def filterOutliersBy[T, M[AA] <: TraversableLike[AA,M[AA]]](xa: M[T], f: T => Double)(implicit bf: CanBuildFrom[Nothing, Double, M[Double]]) : M[T] = {
    val values = xa.map[Double, M[Double]](f)(scala.collection.breakOut)
    val m = mean(values)
    val s = stddev(m, values)
    val outlier = isOutlier(m, s) _
    val filterFn = { t:T =>  outlier(f(t)) == false }
    xa.filter(filterFn)
  }

}
