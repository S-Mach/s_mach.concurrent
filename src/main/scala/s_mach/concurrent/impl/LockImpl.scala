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

import s_mach.concurrent.util.{Semaphore, Lock}

import scala.concurrent.{ExecutionContext, Future}

/**
 * The default implementation of Lock using a Semaphore backend
 */
class LockImpl extends Lock {
  private[this] val semaphore = Semaphore(1)

  override def lock[X](task: => Future[X])(implicit ec: ExecutionContext)= semaphore.acquire(1)(task)
  override def isUnlocked = semaphore.availablePermits > 0
  override def waitQueueLength = semaphore.waitQueueLength
}
