package s_mach.concurrent

import org.scalatest.{FlatSpec, Matchers}
import s_mach.concurrent.util.AtomicFSM

class AtomicFSMTest extends FlatSpec with Matchers {
  "AtomicFSM.current" should "get the current state" in {
    val fsm = AtomicFSM[String,String,Boolean]("initial") {
      case ("initial","1") => "initial1"
      case (s,_) => s
    } {
      case ("initial","initial1") => true
      case _ => false
    }

    fsm.current shouldBe "initial"
  }

  "AtomicFSM.apply" should "atomically transition current state" in {
    val fsm = AtomicFSM[String,String,Boolean]("initial") {
      case ("initial","1") => "initial1"
      case (s,_) => s
    } {
      case ("initial","initial1") => true
      case _ => false
    }

    fsm.current shouldBe "initial"
    fsm("1") shouldBe true
    fsm.current shouldBe "initial1"
    fsm("1") shouldBe false
    fsm.current shouldBe "initial1"
  }

  "AtomicFSM.onTransition" should "should register a callback that is invoked when state transitions" in {
    val fsm = AtomicFSM[String,String,Boolean]("initial") {
      case ("initial","1") => "initial1"
      case (s,_) => s
    } {
      case ("initial","initial1") => true
      case _ => false
    }

    fsm.current shouldBe "initial"
    var count = 0
    fsm.onTransition {
      case ("initial","initial1") =>
        count = count + 1
      case _ =>
        count = count - 1
    }
    fsm.onTransition {
      case ("initial","initial1") =>
        count = count + 1
      case _ =>
        count = count - 1
    }
    fsm("1") shouldBe true
    count shouldBe 2
    fsm("1") shouldBe false
    count shouldBe 2
  }
}
