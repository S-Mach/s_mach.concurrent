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
import scala.util.{Failure, Success, Try}

class SeriallyConfigBuilderTest extends FlatSpec with Matchers with ConcurrentTestCommon {

  "SeriallyConfigBuilder.map-t1" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.map(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal (Success(items))
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }

  "SeriallyConfigBuilder.flatMap-t2" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.flatMap(successN)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(items.flatMap(i => Vector(i,i,i))))
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }


  "SeriallyConfigBuilder.foreach-t3" must "execute each future one at a time" in {
    test repeat TEST_COUNT run {
      implicit val ctc = mkConcurrentTestContext()
      import ctc._

      sched.addEvent("start")

      val result = items.serially.foreach(success)

      waitForActiveExecutionCount(0)
      sched.addEvent("end")

      result.getTry should equal(Success(()))
      val eventMap = sched.eventMap
      (1 to items.size) foreach { i =>
        eventMap.contains(s"success-$i") should equal(true)
      }
      isSerialSchedule(items.size, sched) should equal(true)
    }
  }

}

