package s_mach.concurrent.impl

trait AbstractAsyncTaskRunner extends DelegatingTaskRunner {
  val asyncConfig: AsyncConfig
  import asyncConfig._

  val taskHooks : Seq[TaskHook] = Seq(
    optProgress.map(ProgressState.apply)
  ).flatten

  val taskStepHooks : Seq[TaskStepHook] = Seq(
    optThrottle.map(ThrottleState.apply),
    optRetry.map(RetryState.apply),
    optProgress.map(ProgressState.apply)
  ).flatten
}
