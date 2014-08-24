package s_mach.concurrent.util

/**
 * A case class to report progress in a computation
 * @param completed the count of iterations completed so far
 * @param optTotal Some(total iterations in computation) or None if the computation has an unknown size
 * @param startTime_ns the time in nanoseconds returned by System.nanoTime when the computation was started
 */
case class Progress(
  completed: Long,
  optTotal: Option[Long],
  startTime_ns: Long
)
