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


import org.scalatest.FlatSpec
import s_mach.concurrent.util._
import org.scalatest.Matchers
import TestBuilder._

class LockTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "Lock-lock-t0" must "initially be unlocked" in {
    test repeat TEST_COUNT run {
      val l = Lock()

      l.isUnlocked should equal(true)
      l.waitQueueLength should equal(0)
    }
  }

  "Lock-lock-t1" must "execute immediately if unlocked" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val l = Lock()
      val f1 = l.lock { ().future }
      f1.get should equal(())

      waitForActiveExecutionCount(0)

      l.isUnlocked should equal(true)
      l.waitQueueLength should equal(0)
    }
  }

  "Lock-lock-t2" must "wait if locked" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()

      val l = Lock()
      val latch = Latch()
      l.lock { latch happensBefore 1.future }
      val f2 = l.lock { 2.future }
      l.isUnlocked should equal(false)
      l.waitQueueLength should equal(1)

      f2.isCompleted should equal(false)
      latch.set()
    }
  }

  "Lock-lock-t3" must "wait if locked and execute once unlocked in order of call" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val l = Lock()
      val latch = Latch()
      val f1 = l.lock { sched.addStartEvent("1");latch happensBefore 1.future }
      val f2 = l.lock { sched.addStartEvent("2");2.future }
      val f3 = l.lock { sched.addStartEvent("3");3.future }
      l.isUnlocked should equal(false)
      l.waitQueueLength should equal(2)

      f1.isCompleted should equal(false)
      f2.isCompleted should equal(false)
      f3.isCompleted should equal(false)

      latch.set()
      async.par.run(f1,f2,f3).get

      sched.happensBefore("1", "2") should equal(true)
      sched.happensBefore("2", "3") should equal(true)
    }
  }

  "lock-lock-t4" must "must unlock after executing" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val l = Lock()
      var temp : Any = 0
      l.lock {
        temp = l.isUnlocked
        ().future
      }.get

      // Release happens as reaction Future to above need to wait for it to finish
      waitForActiveExecutionCount(0)

      temp should equal(false)
      l.isUnlocked should equal(true)
      l.waitQueueLength should equal(0)
    }
  }

}
