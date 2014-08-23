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

import scala.concurrent._
import scala.concurrent.duration._

import org.scalatest.{Matchers, FlatSpec}
import util._
import TestBuilder._
import scala.util.{Random, Failure, Success, Try}

class SequencerTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  import TestBuilder._

  "Sequencer" must "ensure Futures are executed in sequential order" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      val s = Sequencer(1)
      s.next should equal(1)

      // Feed futures to Sequencer in a random order
      val result = Random.shuffle(items).map { item =>
        s.when(item)(() => success(item))
      }

      waitForActiveExecutionCount(0)

      s.next should equal(7)

      Future.sequence(result).get.sorted should equal(items)

      (1 to ITEM_COUNT - 1).foreach { item =>
        sched.happensBefore(s"success-${item}",s"success-${item+1}")
      }
    }
  }
}