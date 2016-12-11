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
package s_mach.concurrent

import java.util.concurrent.Executors

import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.impl.ListQueue
import scala.concurrent.{Future, ExecutionContext}

import scala.concurrent.duration._
import util._
import TestBuilder._

import scala.util.Success

class ListQueueTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "ListQueue.poll-t0" must "return a Future that completes once input is available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val q = ListQueue[Int]()
      val future = q.poll()

      future.isCompleted should equal(false)
      q.offer(1)
      future.get(1.millis) should equal(1)
      q.pollQueueSize should equal(0)
      q.offerQueueSize should equal(0)
    }
  }

  "ListQueue.poll-t1" must "return a Future immediately if input is available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val q = ListQueue[Int](Vector(1,2,3,4,5,6,7,8,9,10):_*)
      val future = q.poll()

      future.isCompleted should equal(true)
      future.get(10.millis) should equal(1)
      q.pollQueueSize should equal(0)
      q.offerQueueSize should equal(9)
    }
  }


  "ListQueue.poll(many)-t2" must "return a Future that completes once input is available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val q = ListQueue[Int]()
      val future = q.poll(10)

      q.pollQueueSize should equal(10)
      future.isCompleted should equal(false)

      q.offer(Vector(1,2,3)) // partial
      future.isCompleted should equal(false)
      q.offerQueueSize should equal(3)

      q.offer(Vector(4,5,6,7,8,9,10))
      future.get(10.millis) should equal(Vector(1,2,3,4,5,6,7,8,9,10))
      q.pollQueueSize should equal(0)
      q.offerQueueSize should equal(0)
    }
  }

  "ListQueue.poll(many)-t3" must "return a Future immediately if input is available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val q = ListQueue[Int](Vector(1,2,3,4,5,6,7,8,9,10): _*)
      q.offerQueueSize should equal(10)

      val future = q.poll(10)
      future.isCompleted should equal(true)
      future.get should equal(Vector(1,2,3,4,5,6,7,8,9,10))
      q.pollQueueSize should equal(0)
      q.offerQueueSize should equal(0)
    }
  }

  "ListQueue.offer(many)-t4" must "fulfill all waiting futures in the order they were received" in {
    // Note: using serial executor here to prevent thread processing delays from generating an incorrect
    // serialization schedule when detecting events. Using a concurrent executor causes incorrect serialization
    // about 10% of the time
    val serialTestContext = {
      val serialExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
      val serialScheduledExecutionContext = ScheduledExecutionContext(1)(serialExecutionContext)
      ConcurrentTestContext()(serialExecutionContext, serialScheduledExecutionContext)
    }

    def registerEvent(f: Future[Any], event: String)(implicit ctc:ConcurrentTestContext) =
      f.onComplete { case _ => ctc.sched.addStartEvent(event) }(serialTestContext)

    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val q = ListQueue[Int]()
      val f1 = q.poll()
      registerEvent(f1, "f1")
      val f2 = q.poll(2)
      registerEvent(f2, "f2")
      val f3 = q.poll(6)
      registerEvent(f3, "f3")
      val f4 = q.poll()
      registerEvent(f4, "f4")
      q.pollQueueSize should equal(10)
      q.offerQueueSize should equal(0)

      waitForActiveExecutionCount(0)

      q.offer(Vector(1,2,3,4,5,6,7,8,9,10,11))
      q.pollQueueSize should equal(0)
      q.offerQueueSize should equal(1)

      val result = async.par.run(f1,f2,f3,f4)

      waitForActiveExecutionCount(0)
      serialTestContext.waitForActiveExecutionCount(0)

      result.getTry should equal(Success((1,Vector(2,3),Vector(4,5,6,7,8,9),10)))

      sched.happensBefore("f1", "f2") should equal(true)
      sched.happensBefore("f2", "f3") should equal(true)
      sched.happensBefore("f3", "f4") should equal(true)
    }
  }
}
