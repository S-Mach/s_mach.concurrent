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

import s_mach.concurrent.impl.ConcurrentlyConfig

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._

class ConcurrentlyConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "SeriallyConfigBuilder-t0" must "build and copy config correctly" in {
    implicit val ctc = mkConcurrentTestContext()

    val items = mkItems

    val progressReporter = new ProgressReporter {
      override def onStartTask(): Unit = ???
      override def onCompleteStep(stepId: Long): Unit = ???
      override def onStartStep(stepId: Long): Unit = ???
      override def onCompleteTask(): Unit = ???
    }

    val retryFn = { _:List[Throwable] => false.future }

    val config1Builder =
      items
        .concurrently
        .retry(retryFn)
        .progress(progressReporter)


    config1Builder.ma should equal(items)
    config1Builder.optTotal should equal(Some(items.size))
    config1Builder.optRetry should equal(Some(retryFn))
    config1Builder.optProgress should equal(Some(progressReporter))

    val config2Builder =
      new Traversable[Int] {
        override def hasDefiniteSize: Boolean = false
        override def foreach[U](f: Int => U): Unit = ???
      }.concurrently

    config2Builder.optTotal should equal(None)

    val config1 = config1Builder.build()
    val config2 = items.concurrently.using(config1).build()

    config1 should equal(config2)

    val config3 = ConcurrentlyConfig(
      optProgress = config2.optProgress,
      optRetry = config2.optRetry
    )

    config3 should equal(config1)
    config3 should equal(config2)
  }

  "ConcurrentlyConfigBuilder.map-t1" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        val items = mkItems
        val result = items.concurrently.map(success)

        waitForActiveExecutionCount(0)

        result.getTry should equal (Success(items))
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "ConcurrentlyConfigBuilder.flatMap-t2" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val items = mkItems
        val result = items.concurrently.flatMap(successN)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "ConcurrentlyConfigBuilder.foreach-t3" must "execute each future at the same time" in {
    val result =
      test repeat TEST_COUNT run {
        implicit val ctc = mkConcurrentTestContext()
        import ctc._

        sched.addEvent("start")

        val items = mkItems
        val result = items.concurrently.foreach(success)

        waitForActiveExecutionCount(0)
        sched.addEvent("end")

        result.getTry should equal(Success(()))
        val eventMap = sched.eventMap
        items foreach { i =>
          eventMap.contains(s"success-$i") should equal(true)
        }
        isConcurrentSchedule(items, sched)
      }

    val concurrentPercent = result.count(_ == true) / result.size.toDouble
    concurrentPercent should be >= MIN_CONCURRENCY_PERCENT
  }

  "ConcurrentlyConfigBuilder.modifiers-t4" must "execute each future one at a time and apply retry and progress correctly" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val items = mkItems
      val allAttempts = Array.fill(items.size)(1)

      val result =
        items
          .zipWithIndex
          .concurrently
          .retry {
            case List(r:RuntimeException) =>
              sched.addEvent(s"retry-${r.getMessage}")
              true.future
            case List(r1:RuntimeException,r2:RuntimeException) =>
              sched.addEvent(s"retry-${r1.getMessage}")
              true.future
            case _ => false.future
          }
          .progress(new ProgressReporter {
            override def onStartTask() = sched.addEvent(s"progress-start")
            override def onCompleteTask() = sched.addEvent(s"progress-end")
            override def onStartStep(stepId: Long) = { }
            override def onCompleteStep(stepId: Long) = sched.addEvent(s"progress-$stepId")
          })
          .map { case (i,idx) =>
            val attempts = allAttempts(idx)
            Future {
              if(attempts < 3) {
                allAttempts(idx) += 1
                sched.addEvent(s"map-$i+$attempts")
                throw new RuntimeException(s"$i+$attempts")
              } else {
                sched.addEvent(s"map-$i+$attempts")
                i
              }
            }
          }

      result.get
      // TODO: this doesn't work properly below 1 ms throttle?
//      waitForActiveExecutionCount(0)

      items.zipWithIndex foreach { case (i,idx) =>
        sched.happensBefore(s"progress-start",s"map-$i+1") should equal(true)
        sched.happensBefore(s"map-$i+1",s"retry-$i+1") should equal(true)
        sched.happensBefore(s"retry-$i+1",s"map-$i+2") should equal(true)
        sched.happensBefore(s"map-$i+2",s"retry-$i+2") should equal(true)
        sched.happensBefore(s"map-$i+3",s"progress-$idx") should equal(true)
        sched.happensBefore(s"progress-$idx",s"progress-end") should equal(true)
      }
    }
  }

}

