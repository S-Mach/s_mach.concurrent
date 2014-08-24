package s_mach.concurrent

import scala.concurrent.duration.Duration
import s_mach.concurrent.util.Barrier

/**
 * A trait for a task that after the initial delay expires, is repeatedly started with a specified period in the
 * background. This will continue until the task is cancelled or a failure occurs.
 */
trait PeriodicTask {
  /** @return the time stamp in nanoseconds when the task will be started */
  def nextEvent_ns : Long
  /** @return the delay before initially starting the task */
  def initialDelay : Duration
  /** @return the delay between the starting of the task */
  def period : Duration
  /** @return TRUE if the task was cancelled FALSE if the task was already cancelled */
  def cancel() : Boolean
  /** @return a barrier that is set once the task is cancelled */
  def onCancel : Barrier
}