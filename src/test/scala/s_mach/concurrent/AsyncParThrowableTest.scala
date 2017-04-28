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

import org.scalatest.{Matchers, FlatSpec}

class AsyncParThrowableTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "AsyncParThrowable" must "wait on all Futures to complete concurrently" in {
    val ex1 = new RuntimeException("1")
    val ex2 = new RuntimeException("2")
    val ex3 = new RuntimeException("3")
    val t = AsyncParThrowable(
      firstFailure = ex1,
      allFailure = Vector(ex2,ex3).future
    )

    t.firstFailure should equal(ex1)
    t.allFailure.await should equal(Vector(ex2,ex3))


    t.getMessage should equal(ex1.getMessage)
    t.getLocalizedMessage should equal(ex1.getLocalizedMessage)
    t.getCause should equal(ex1)
    an [UnsupportedOperationException] should be thrownBy t.initCause(new RuntimeException)
    t.toString should equal("AsyncParThrowable(java.lang.RuntimeException: 1)")
  }
}
