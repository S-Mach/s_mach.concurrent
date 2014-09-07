package s_mach.concurrent

import org.scalatest.{Matchers, FlatSpec}

class ConcurrentThrowableTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "ConcurrentThrowable" must "wait on all Futures to complete concurrently" in {
    val ex1 = new RuntimeException("1")
    val ex2 = new RuntimeException("2")
    val ex3 = new RuntimeException("3")
    val t = ConcurrentThrowable(
      firstFailure = ex1,
      allFailure = Vector(ex2,ex3).future
    )

    t.firstFailure should equal(ex1)
    t.allFailure.get should equal(Vector(ex2,ex3))


    t.getMessage should equal(ex1.getMessage)
    t.getLocalizedMessage should equal(ex1.getLocalizedMessage)
    t.getCause should equal(ex1)
    an [UnsupportedOperationException] should be thrownBy t.initCause(new RuntimeException)
    t.toString should equal("ConcurrentThrowable(java.lang.RuntimeException: 1)")
  }
}
