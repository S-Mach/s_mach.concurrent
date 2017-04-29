package s_mach.concurrent

import org.scalatest.{FlatSpec, Matchers}
import s_mach.concurrent.util.AtomicState

class AtomicStateTest extends FlatSpec with Matchers {
  "AtomicState.current" should "should get the current state" in {
    val state = AtomicState[String]("initial")
    state.current shouldBe "initial"
  }

  "AtomicState.set" should "should set the current state and return it" in {
    val state = AtomicState[String]("initial")
    state.current shouldBe "initial"
    state.set {
      case "initial" => "initial1"
      case s => s
    } shouldBe "initial1"
    state.current shouldBe "initial1"
  }

  "AtomicState.setAndThen" should "should set the current state and then call after transition exactly once" in {
    val state = AtomicState[String]("initial")
    state.current shouldBe "initial"
    var count = 0
    state.setAndThen {
      case "initial" => "initial1"
      case s => s
    } {
      case ("initial","initial1") =>
        count = count + 1
        true
      case _ =>
        count = count + 1
        false
    } shouldBe true
    state.current shouldBe "initial1"
    count shouldBe 1
  }

  "AtomicState.maybeSet" should "should set the current state if defined and return true" in {
    val state = AtomicState[String]("initial")
    state.current shouldBe "initial"
    state.maybeSet {
      case "initial" => "initial1"
    } shouldBe true
    state.current shouldBe "initial1"
  }

  "AtomicState.maybeSet" should "should not set the current state if undefined and return false" in {
    val state = AtomicState[String]("initial")
    state.current shouldBe "initial"
    state.maybeSet {
      case "initial1" => "initial2"
    } shouldBe false
    state.current shouldBe "initial"
  }
}
