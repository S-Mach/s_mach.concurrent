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
package s_mach.concurrent

import java.util.concurrent.Executors

import org.scalatest.exceptions.TestFailedException
import s_mach.concurrent.util.ConcurrentTestContext

import scala.util.{Try, Failure}

object TestBuilder {
  def summarizeFailures(repeat: Int, allowedFailureCount: Int, failures: Seq[Throwable]) : Throwable = {
    val passedCount = repeat - failures.size
    val passedPercent = "%.1f" format (passedCount * 100 / repeat.toDouble)
    val output =
      s"More than $allowedFailureCount failing tests detected!" + "\n" +
      s"$passedCount of $repeat tests passed validation ($passedPercent%)" + "\n" +
      s"First 5 failing tests:" + "\n" +
      failures.take(5).zipWithIndex.map {
        case (failure:TestFailedException, i) => s"[${i+1}] ${failure.getMessage()} (${failure.failedCodeFileNameAndLineNumberString.getOrElse("no filename/linenumber!")})"
        case (failure,i) => s"[${i+1}]$failure"
      }.mkString("\n")

    // Wrap in a TestFailedException for display in sbt
    failures.head match {
      case failure:TestFailedException =>
        new TestFailedException(
          messageFun = { _ => Some(output) },
          cause = Some(new RuntimeException(output, failure.getCause)),
          failedCodeStackDepthFun = { _ => 0 },
          payload = None
        ) {
          override def failedCodeFileNameAndLineNumberString = None
        }
      case failure => failure
    }
  }

  def repeatTest[R](repeat: Int, allowedFailureCount: Int)(f: => R) : IndexedSeq[R] = {
    println(Thread.currentThread().getStackTrace.tail.find(_.getFileName != "TestBuilder.scala").get)
    val startTime_ms = System.currentTimeMillis()
    val bs = "\b" * 6
    val results = (0 until repeat).map { i =>
      val retv = Try(f)
      if(i % 10 == 0) {
        print(bs)
        print("%5.1f%%" format (i * 100.0/repeat.toDouble))
      }
      retv
    }
    println(s"${bs}100.0%")
    val elapsed_ms = System.currentTimeMillis() - startTime_ms
    val testsPerSec = (repeat.toDouble/elapsed_ms*1000.0).toInt
    println(s"Completed $repeat tests in $elapsed_ms ms ($testsPerSec tests/sec)")
    val failures = results.collect { case Failure(t) => t }
    if(failures.nonEmpty) {
      val failureSummary = summarizeFailures(repeat, allowedFailureCount, failures)
      throw failureSummary
    } else {
      results.map(_.get)
    }
  }

  trait ContextTestBuilder[C] {
    def run[R](f: C => R) : IndexedSeq[R]
  }

  trait TestBuilder extends ContextTestBuilder[Unit] {
    def run[R](f: => R) : IndexedSeq[R] = run({ Unit => f })
  }

  implicit class PimpMyTestBuilder(val self: TestBuilder) {
    def context[C](context: C) = {
      new ContextTestBuilder[C] {
        def run[R](f: C => R) = self.run { Unit => f(context) }
      }
    }

    def repeat(repeat: Int, allowedFailureCount: Int = 0) : TestBuilder = {
      val _repeat = repeat
      new TestBuilder {
        def run[R](f: Unit => R) = {
          self.run[IndexedSeq[R]] { _:Unit =>
            repeatTest(_repeat, allowedFailureCount)(f(()))
          }.flatten
        }
      }
    }

    def repeat(repeat: Int, allowedFailurePercent: Double) : TestBuilder = {
      val _repeat = repeat
      val allowedFailureCount = (_repeat * allowedFailurePercent).toInt
      this.repeat(_repeat, allowedFailureCount)
    }
  }

  val test = new TestBuilder {
    override def run[R](f: (Unit) => R) = IndexedSeq(f(()))
  }

  implicit class PimpMyIndexedSeq[R](val result: IndexedSeq[R]) {
    def validate(f: IndexedSeq[R] => Unit) = f(result)
  }

}
