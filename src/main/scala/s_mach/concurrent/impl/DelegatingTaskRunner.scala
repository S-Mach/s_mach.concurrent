/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____        __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.impl

import scala.concurrent.{ExecutionContext, Future}

trait DelegatingTaskRunner extends TaskRunner {
  def taskHooks : Seq[TaskHook]
  def taskStepHooks : Seq[TaskStepHook]

  override def hookStepFunction0[R](step: (TaskStepId) => Future[R])(implicit ec:ExecutionContext): (TaskStepId) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction0(step))
  }

  override def hookStepFunction1[A, R](step: (TaskStepId, A) => Future[R])(implicit ec:ExecutionContext): (TaskStepId, A) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction1(step))
  }

  override def hookStepFunction2[A, B, R](step: (TaskStepId, A, B) => Future[R])(implicit ec:ExecutionContext): (TaskStepId, A, B) => Future[R] = {
    taskStepHooks.foldLeft(step)((step, taskStepHook) => taskStepHook.hookStepFunction2(step))
  }

  override def hookTask[R](task: () => Future[R])(implicit ec:ExecutionContext): () => Future[R] = {
    taskHooks.foldLeft(task)((task, taskHook) => taskHook.hookTask(task))
  }
}