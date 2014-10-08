package s_mach.concurrent.impl

case class AsyncTaskRunner(cfg: AsyncConfig) extends DelegatingTaskRunner {
  import cfg._

  val taskHooks : Seq[TaskHook] = Seq(
    optProgress.map(ProgressState.apply)
  ).flatten

  val taskStepHooks : Seq[TaskStepHook] = Seq(
    optThrottle.map(ThrottleState.apply),
    optRetry.map(RetryState.apply),
    optProgress.map(ProgressState.apply)
  ).flatten
}

