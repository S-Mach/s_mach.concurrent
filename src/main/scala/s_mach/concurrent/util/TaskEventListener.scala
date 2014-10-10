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
package s_mach.concurrent.util

/**
 * A trait for listening to the progress of a task that consists of one or more
 * discrete steps. Each step is identified by an  ordinal step identifier. The
 * step id is a generic concept and may be mapped by callers to any concrete
 * concept. For  tasks with a known fixed input size, the step id is mapped
 * directly to the index of the input (i.e. input with index  0 = step 1, index
 * 1 = step 2, etc). For tasks with an unknown input size, the step id is mapped
 * to the Nth item  encountered (i.e. 1st item = step 1, 2nd item = step 2). The
 * step id must be incrementally assigned,  ensuring that that total steps
 * always equals last step id.
 *
 * Note: derived task event listeners are assumed to be stateful.
 * Implementations must be thread safe
 */
trait TaskEventListener {
  /** Called at the beginning of the computation */
  def onStartTask() : Unit
  /** Called once the computation completes */
  def onCompleteTask() : Unit
  /** Called at the beginning of execution of a step of the computation */
  def onStartStep(stepId: Int) : Unit
  /** Called at the beginning of execution of a step of the computation */
  def onCompleteStep(stepId: Int) : Unit
}