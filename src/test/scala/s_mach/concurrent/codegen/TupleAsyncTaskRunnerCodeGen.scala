/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____       __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2017 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.codegen

object TupleAsyncTaskRunnerCodeGen {
  def gen(n: Int) = {
    val lcs = ('a' to 'z').map(_.toString).take(n)
    val ucs = ('A' to 'Z').map(_.toString).take(n)
    val allUcs = ucs.mkString(",")
    val allLcs = lcs.mkString(",")
s"""
case class Tuple${n}AsyncTaskRunner(asyncConfig: AsyncConfig) extends AbstractAsyncTaskRunner {
  import asyncConfig._

  def run[$allUcs](
    ${(0 until n).map(i => s"f${lcs(i)}: => Future[${ucs(i)}]").mkString(",\n    ")}
  )(implicit ec:ExecutionContext) : Future[($allUcs)] = {
    ${(0 until n).map { i =>
      val lc = lcs(i)
      s"val wf$lc = { () => hookStepFunction0 { stepId:Int => f$lc }.apply(${i+1}) }"
    }.mkString("\n    ")}
    hookTask { () =>
      val promise = Promise[($allUcs)]()
      val semaphore = Semaphore(workerCount)
      ${(0 until n).map { i =>
        val lc = lcs(i)
        s"val f$lc = semaphore.acquire(1)(wf$lc())" 
      }.mkString("\n      ")}
      mergeFailImmediately(promise, Vector(${lcs.map(lc => s"f$lc").mkString(",")}))
      val future =
        for {
          ${(0 until n).map { i =>
            val lc = lcs(i)
            s"$lc <- f$lc"
          }.mkString("\n          ")}
        } yield ($allLcs)
      future foreach { t => promise.success(t) }
      promise.future
    }.apply()
  }
}
"""
  }
  
  def genToFile(path: String) : Unit = {
    val contents =
s"""package s_mach.concurrent.impl

/* WARNING: Generated code. To modify see s_mach.concurrent.codegen.TupleAsyncTaskRunnerCodeGen */

import s_mach.concurrent.util.Semaphore
import scala.concurrent.{ExecutionContext, Future, Promise}
import s_mach.concurrent.impl.MergeOps.mergeFailImmediately
import s_mach.concurrent.config.AsyncConfig

trait SMach_Concurrent_AbstractPimpMyAsyncConfig extends Any {
  def self: AsyncConfig

  ${(2 to 22).map { n =>
    val lcs = ('a' to 'z').map(_.toString).take(n)
    val ucs = ('A' to 'Z').map(_.toString).take(n)
    val allUcs = ucs.mkString(",")
//    val allLcs = lcs.mkString(",")

s"""
  def run[$allUcs](
    ${(0 until n).map(i => s"f${lcs(i)}: => Future[${ucs(i)}]").mkString(",\n    ")}
  )(implicit ec:ExecutionContext) : Future[($allUcs)] = {
    Tuple${n}AsyncTaskRunner(self).run(${lcs.map(lc => s"f$lc").mkString(",")})
  }
"""
  }.mkString("\n")}
}

${(2 to 22).map(i => gen(i)).mkString("\n")}
"""
    
    import java.io._
    val out = new PrintWriter(new BufferedWriter(new FileWriter(path)))
    out.println(contents)
    out.close()
  }

}
