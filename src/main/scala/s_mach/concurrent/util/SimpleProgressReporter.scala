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

    override def onStartProgress(): Unit = {
      lock.synchronized {
        totalSoFar = 0
        startTime_ns = System.nanoTime()
        report(Progress(0, optTotal, startTime_ns))
      }
    }


    override def onEndProgress(): Unit = { }

    /**
     * Accumulate completed and report progress
     * @param completed count of operations completed
     */
    def apply(completed: Long) : Unit = {
      // Note: lock is required here to ensure proper ordering of very fast reports
      lock.synchronized {
        totalSoFar += completed
        report(Progress(totalSoFar, optTotal, startTime_ns))
      }
    }
  }
}

