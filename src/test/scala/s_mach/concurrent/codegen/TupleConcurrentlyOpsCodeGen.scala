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
package s_mach.concurrent.codegen

object TupleConcurrentlyOpsCodeGen {
  def gen(n: Int) = {
    val lcs = ('a' to 'z').map(_.toString).take(n)
    val ucs = ('A' to 'Z').map(_.toString).take(n)
    val allUcs = ucs.mkString(",")
    val allLcs = lcs.mkString(",")
s"""
  /**
   * @return A Future of a tuple of all results that completes once all futures complete
   */
  def concurrently[$allUcs,ZZ](
    ${
    (0 until n)
      .map { i => s"f${lcs(i)}: Future[${ucs(i)}]" }
      .mkString(",\n    ")
}
  )(implicit
    ec:ExecutionContext
  ) : Future[($allUcs)] = {
    val promise = Promise[($allUcs)]()
    mergeFailImmediately(promise, Vector(${lcs.map(l => s"f$l").mkString(",")}))
    val future =
      for {
        ${lcs.map(l => s"$l <- f$l").mkString("\n        ")}
      } yield ($allLcs)

    future onSuccess { case v => promise.trySuccess(v) }
    promise.future
  }
"""
  }
  
  def genToFile(path: String) : Unit = {
    val contents =
s"""package s_mach.concurrent

/* WARNING: Generated code. To modify see TupleConcurrentlyOpsCodeGen */

import scala.concurrent.{ Future, ExecutionContext, Promise }
import MergeOps.mergeFailImmediately

object TupleConcurrentlyOps extends TupleConcurrentlyOps
trait TupleConcurrentlyOps {
  ${(2 to 22).map(i => gen(i)).mkString("\n")}
}
"""    
    
    import java.io._
    val out = new PrintWriter(new BufferedWriter(new FileWriter(path)))
    out.println(contents)
    out.close()
  }

}
