package s_mach.concurrent.util

/**
 * A trait for a simple progress reporter that accumulates total progress from completed reports and makes a report
 * for each completed report
 */
trait SimpleProgressReporter extends ProgressReporter

object SimpleProgressReporter {
  def apply(
    optTotal: Option[Long],
    report: Progress => Unit
  ) : SimpleProgressReporter = new SimpleProgressReporterImpl(
    optTotal = optTotal,
    report = report
  )

  class SimpleProgressReporterImpl(
    optTotal: Option[Long],
    report: Progress => Unit
  ) extends SimpleProgressReporter {
    val lock = new Object
    var totalSoFar = 0l
    var startTime_ns = 0l

    override def onStartTask(): Unit = {
      lock.synchronized {
        totalSoFar = 0
        startTime_ns = System.nanoTime()
        report(Progress(0, optTotal, startTime_ns))
      }
    }


    override def onCompleteTask(): Unit = { }

    override def onStartStep(stepId: Long) = { }

    def onCompleteStep(stepId: Long) : Unit = {
      // Note: lock is required here to ensure proper ordering of very fast reports
      lock.synchronized {
        totalSoFar += 1
        report(Progress(totalSoFar, optTotal, startTime_ns))
      }
    }
  }
}

