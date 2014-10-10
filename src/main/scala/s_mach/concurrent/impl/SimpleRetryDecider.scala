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
import s_mach.concurrent.util.RetryDecider

case class SimpleRetryDecider(
  f: List[Throwable] => Future[Boolean]
)(implicit ec:ExecutionContext) extends RetryDecider {
  val failures = new java.util.concurrent.ConcurrentHashMap[Long, List[Throwable]]
  override def shouldRetry(
    taskStepId: Long,
    failure: Throwable
  ): Future[Boolean] = {
    // Note: this test/execute is not atomic but this is ok since access to a
    // particular step will always be synchronous
    val newFailList = failure :: Option(failures.get(taskStepId)).getOrElse(Nil)
    failures.put(taskStepId, newFailList)
    f(newFailList)
  }
}

