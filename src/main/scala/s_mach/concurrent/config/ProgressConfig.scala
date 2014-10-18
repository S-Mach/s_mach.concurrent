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
package s_mach.concurrent.config

import s_mach.concurrent.util.TaskEventListener

import scala.concurrent.ExecutionContext

/**
 * A trait for configuring optional progress reporting
 */
trait OptProgressConfig {
  def optTotal: Option[Int]

  def optProgress: Option[ProgressConfig]
}

/**
 * A trait that configures progress reporting
 */
trait ProgressConfig {
  implicit def executionContext: ExecutionContext
  def reporter: TaskEventListener
}

object ProgressConfig {
  case class ProgressConfigImpl(
    optTotal: Option[Int],
    reporter: TaskEventListener
  )(implicit
    val executionContext: ExecutionContext
  ) extends ProgressConfig

  def apply(
    optTotal: Option[Int],
    reporter: TaskEventListener
  )(implicit
    executionContext: ExecutionContext
  ) : ProgressConfig = ProgressConfigImpl(optTotal, reporter)
}

