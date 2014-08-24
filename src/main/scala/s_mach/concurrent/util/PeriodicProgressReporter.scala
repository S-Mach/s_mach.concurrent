package s_mach.concurrent.util

import s_mach.concurrent.{PeriodicTask, ScheduledExecutionContext}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * A trait for a progress reporter that reports only at the specified report interval
 */
trait PeriodicProgressReporter extends ProgressReporter {
  def reportInterval: Duration
}

object PeriodicProgressReporter {
  def apply(
    optTotal: Option[Long],
    reportInterval: Duration,
    report: Progress => Unit
  )(implicit
    executionContext: ExecutionContext,
    scheduledExecutionContext: ScheduledExecutionContext
  ) : PeriodicProgressReporter = new PeriodicProgressReporterImpl(
    optTotal = optTotal,
    reportInterval = reportInterval,
    report = report
  )

  class PeriodicProgressReporterImpl(
    optTotal: Option[Long],
    val reportInterval: Duration,
    report: Progress => Unit
  )(implicit
    executionContext: ExecutionContext,
    scheduledExecutionContext: ScheduledExecutionContext
  ) extends PeriodicProgressReporter {
    val totalSoFar = new java.util.concurrent.atomic.AtomicLong(0)

    val lock = new Object
    var startTime_ns = 0l
    var lastReport_ns = 0l
    var reporter : Option[PeriodicTask] = None

    override def onStartProgress() = {
      lock.synchronized {
        reporter = Some(
          scheduledExecutionContext.scheduleAtFixedRate(
            reportInterval,
            reportInterval
          ) { () =>
            doReport(totalSoFar.get)
          }
        )
        val now = System.nanoTime()
        startTime_ns = now
        lastReport_ns = now
        report(Progress(0, optTotal, startTime_ns))
      }
    }

    override def onEndProgress() = {
      lock.synchronized {
        require(reporter != None)
        require(optTotal.forall(_ == totalSoFar.get))

        report(Progress(totalSoFar.get, optTotal, startTime_ns))
        reporter.get.cancel()
        reporter = None
      }
    }

    def doReport(localTotalSoFar: Long) {
      lock.synchronized {
        // Note: is possible for a report to be queued on the lock while onEnd is in progress
        if(reporter != None) {
          lastReport_ns = System.nanoTime()
          report(Progress(localTotalSoFar, optTotal, startTime_ns))
        }

      }
    }

    def apply(completed: Long) = {
      totalSoFar.addAndGet(completed)
    }
  }
}
