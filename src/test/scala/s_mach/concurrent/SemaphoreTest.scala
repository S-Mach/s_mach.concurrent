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

import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.util._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try
import scala.concurrent.duration._


class SemaphoreTest extends FlatSpec with Matchers with ConcurrentTestCommon{
  import TestBuilder._

  "semaphore-t0" must "have all permits available after construction" in {
    test repeat TEST_COUNT run {
      val s = Semaphore(10)
      s.availablePermits should equal(10)
      s.maxAvailablePermits should equal(10)
      s.waitQueueLength should equal(0)
    }
  }

  "semaphore-acquire-t1" must "acquire immediately if permits are available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val s = Semaphore(10)
      val f1 = s.acquire(10) { ().future }
      f1.get should equal(())

      waitForActiveExecutionCount(0)

      s.availablePermits should equal(10)
      s.maxAvailablePermits should equal(10)
      s.waitQueueLength should equal(0)
    }
  }

  "semaphore-acquire-t2" must "wait if no permits are available" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val s = Semaphore(10)
      val latch = Latch()
      val f1 = s.acquire(1) { latch happensBefore 1.future }
      val f2 = s.acquire(10) { 2.future }
      s.availablePermits should equal(0)
      s.waitQueueLength should equal(1)

      f2.isCompleted should equal(false)
      latch.set()
    }
  }

  "semaphore-acquire-t3" must "wait if no permits are available and execute once they become available in order of call" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val s = Semaphore(10)
      val latch = Latch()
      val f1 = s.acquire(1) { sched.addStartEvent("1");latch happensBefore 1.future }
      val f2 = s.acquire(10) { sched.addStartEvent("2");2.future }
      val f3 = s.acquire(10) { sched.addStartEvent("3");3.future }
      s.availablePermits should equal(0)
      s.waitQueueLength should equal(2)

      f1.isCompleted should equal(false)
      f2.isCompleted should equal(false)
      f3.isCompleted should equal(false)

      latch.set()
      concurrently(f1,f2,f3).get

      sched.happensBefore("1", "2") should be(true)
      sched.happensBefore("2", "3") should be(true)
    }
  }

  "semaphore-acquire-t4" must "throw IllegalArgumentException if more permits are requested than max permits" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._
      val s = Semaphore(10)
      Try(s.acquire(20) { ().future }).failed.get shouldBe a[IllegalArgumentException]
    }
  }

  "permit-release-t5" must "release acquired permits" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._
      val s = Semaphore(10)
      var temp = 0l
      s.acquire(5) {
        temp = s.availablePermits
        ().future
      }.get

      // Release happens as reaction Future to above need to wait for it to finish
      waitForActiveExecutionCount(0)

      temp should equal(5)
      s.availablePermits should equal(10)
      s.waitQueueLength should equal(0)
    }
  }

  "permit-release-t6" must "release acquired permits in order of call" in {
   // Note: using serial executor here to prevent thread processing delays from generating an incorrect
    // serialization schedule when detecting events. Using a concurrent executor causes incorrect serialization
    // about 10% of the time
//    val serialExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
//    def registerEvent(f: Future[Any], event: String)(implicit ctc:ConcurrentTestContext) =
//      f.onComplete { case _ => ctc.sched.startEvent(event) }(serialExecutionContext)

    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val s = Semaphore(10)
      val latch = Latch()

      val f1 = s.acquire(10) { sched.addStartEvent("1");latch.future }
      val f2 = s.acquire(8) { sched.addStartEvent("2"); 2.future }
      val f3 = s.acquire(2) { sched.addStartEvent("3"); 3.future }
      val f4 = s.acquire(1) { sched.addStartEvent("4");4.future }

      latch.set()

      waitForActiveExecutionCount(0)

      sched.happensBefore("1", "2") should equal(true)
      // TODO: why are 2 and 3 in a race condition?
//      sched.happensBefore("2", "3") should equal(true)
      sched.happensBefore("3", "4") should equal(true)
    }
  }
}
