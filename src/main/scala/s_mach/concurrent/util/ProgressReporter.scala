package s_mach.concurrent.util

trait ProgressReporter extends (Long => Unit) {
  def onStart() : Unit
  def onEnd() : Unit
}
