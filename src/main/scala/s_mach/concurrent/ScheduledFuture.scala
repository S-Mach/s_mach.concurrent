package s_mach.concurrent

import s_mach.concurrent.util.Barrier

import scala.concurrent._
import scala.concurrent.duration._

trait DelayedFuture[A] extends Future[A] {
  def nextEvent_ns : Long
  def delay : Duration
  def cancel() : Boolean
}

trait PeriodicTask {
  def nextEvent_ns : Long
  def initialDelay : Duration
  def period : Duration
  def cancel() : Boolean
  def onCancel : Barrier
}