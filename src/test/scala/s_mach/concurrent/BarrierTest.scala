package s_mach.concurrent

import org.scalatest.{Matchers, FlatSpec}
import s_mach.concurrent.util.Barrier

import scala.concurrent.Future

class BarrierTest extends FlatSpec with Matchers with ConcurrentTestCommon {
  "Barrier.set" must "already be set" in {
    implicit val ctc = mkConcurrentTestContext()

    Barrier.set.isSet should equal(true)
    Barrier.set.future.get should equal(())
    Barrier.set.onSet(1).get should equal(1)
    Barrier.set.happensBefore(Future(1)).get should equal(1)
  }
}
