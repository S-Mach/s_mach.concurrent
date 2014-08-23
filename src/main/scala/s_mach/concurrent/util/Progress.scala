package s_mach.concurrent.util


case class Progress(
  completed: Long,
  optTotal: Option[Long],
  startTime_ns: Long
)
