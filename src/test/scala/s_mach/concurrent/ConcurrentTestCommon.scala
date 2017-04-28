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
package s_mach.concurrent

import java.util.concurrent.Executors

import org.scalatest.{Tag, Matchers}
import s_mach.concurrent.util.{ConcurrentTestContext, SerializationSchedule}
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object ConcurrentTestCommon {
  // Not going to worry about shutdown since only one is ever created here
  implicit val scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors())
  println(s"Found ${Runtime.getRuntime.availableProcessors()} CPUs")
}

trait ConcurrentTestCommon extends Matchers {
  // Note: these tests will fail unless there is at least two cores
  assert(Runtime.getRuntime.availableProcessors() > 1)

  val TEST_COUNT = 10000

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

//  val ITEM_COUNT = 6
//  val items = (1 to ITEM_COUNT).toVector
  def mkItems = {
    val g = Random.nextGaussian()
    val n = Math.min(100, Math.max(10, (50 + (50 * g)).toInt))

    val builder = Vector.newBuilder[Int]
    for(i <- 1 to n) {
      builder += Random.nextInt()
    }
    builder.result().distinct
  }

//  val MIN_CONCURRENCY_PERCENT = 0.66
//  val MIN_CONCURRENCY_PERCENT = 0.55
  val MIN_CONCURRENCY_PERCENT = 0.4

  // Fuzz delays to generate more random serialization schedules
  def calcDelay_ns() =  DELAY_NS + (RANDOM_NS * Random.nextGaussian()).toLong

  def success(i: Int)(implicit ctc: ConcurrentTestContext) = {
    import ctc._

    sched.addStartEvent(s"success-$i")
    Future {
      delay(calcDelay_ns())
      sched.addEndEvent(s"success-$i")
      i
    }
  }

  def successN(i: Int)(implicit ctc: ConcurrentTestContext) = {
    import ctc._

    sched.addStartEvent(s"success-$i")
    Future {
      delay(calcDelay_ns())
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

  def isConcurrentSchedule(items: Vector[Int], sched: SerializationSchedule[String]) : Boolean = {
    var found = false
    var done = false
    var i = 0
    var j = 0
    while(found == false && done == false) {
      if(i != j) {
        found = sched.happensDuring(s"success-${items(i)}",s"success-${items(j)}")
      }
      j = j + 1
      if(j == items.size) {
        i = i + 1
        if(i == items.size) done = true
        j = 0
      }
    }
    found
  }

  def isSerialSchedule(items: Vector[Int], sched: SerializationSchedule[String]) : Boolean = {
      // Ensure serial execution
      sched.happensBefore("start",s"success-${items.head}") &&
      (0 until items.size - 1).forall { i =>
        sched.happensBefore(s"success-${items(i)}",s"success-${items(i+1)}")
      } &&
      sched.happensBefore(s"success-${items.last}","end") &&
      isConcurrentSchedule(items, sched) == false
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
