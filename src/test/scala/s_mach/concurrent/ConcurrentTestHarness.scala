///*
//                    ,i::,
//               :;;;;;;;
//              ;:,,::;.
//            1ft1;::;1tL
//              t1;::;1,
//               :;::;               _____        __  ___              __
//          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
//         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
//         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
//       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
//       Lft1,:;:       , 1tfL:
//       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
//         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
//         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
//          .L1 1tt1ttt,,Li
//            ...1LLLL...
//*/
//package s_mach.concurrent
//
//import java.lang.management._
//import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}
//import org.scalatest.{Matchers, FlatSpec}
//import org.scalatest.exceptions.TestFailedException
//import scala.collection.TraversableLike
//import scala.collection.generic.CanBuildFrom
//import scala.collection.mutable.ArrayBuffer
//import scala.concurrent._
//import scala.concurrent.duration._
//import scala.util.Try
//import s_mach.concurrent.util._
//
///**
// * A test harness for testing concurrent code.
// * @tparam R
// */
//trait ConcurrentTestHarness[R] extends FlatSpec with Matchers { self =>
//  import s_mach.concurrent.ConcurrentTestHarness._
//  // Note: these tests will fail unless there is at least two cores
//  assert(Runtime.getRuntime.availableProcessors() > 1)
//
//  /** @return name of the test (for error reporting) */
//  def name: String
//  /** @return the number of times to run the test method */
//  def testCount : Int
//  /** @return the number of times to run the test method with no delay to establishing the average no delay run time,
//    *         force class loading and warmup JIT */
//  def warmupTestCount = 0
//  /** @return the number of worker threads */
//  def workerThreadPoolSize = Runtime.getRuntime.availableProcessors() * 2
//  /** @return the maximum amount of nanoseconds to wait for the test method to complete before throwing TimeOutException */
//  def maxWait_ns = Long.MaxValue
//  /** @return the maximum number of times to attempt to shutdown the executor */
//  def maxExecutorShutdownAttempt = 3
//  /** @return greater than 0 to emit debug messages to stdout. 1 - emit test summary messages. 2 - emit per test messages */
//  def debugMode = 0
//  /** @return the percentage of tests that are allowed to fail */
//  def allowedFailingTestPercent = 0.0
//  /** @return the number of tests that are allowed to fail */
//  def allowedFailingTestCount = (allowedFailingTestPercent * testCount).toInt
//
//  abstract class Test(implicit val ctc: ConcurrentTestContext) {
//    import ctc._
//
//    def onBeforeTest() : Unit = {
//      sched.startEvent("start")
//    }
//
//    /**
//     * Main test method, implemented by derived class.
//     * @return a future of the result
//     * */
//    def test() : Future[R]
//
//    def onAfterTest() : Unit = {
//      sched.startEvent("end")
//    }
//  }
//  def mkTest()(implicit ctc: ConcurrentTestContext) : Test
//
//  /**
//   * Result validation method
//   * */
//  def validateResult(result: Try[R]) : Unit
//
//  /**
//   * Schedule validation method
//   * */
//  def validateSchedule(sched: SerializationSchedule[String]) = { }
//
//  /**
//   * Validate all test results
//   * */
//  def validateResults(results: Vector[(TestResult[R], Option[Throwable])]) : Unit = {
//    if(results.count(_._2.nonEmpty) > allowedFailingTestCount) {
//      val failures = results.collect { case (result, optThrowable) if optThrowable.nonEmpty => (result, optThrowable.get) }
//      val passedCount = testCount - failures.size
//      val passedPercent = "%.1f" format (passedCount * 100 / testCount.toDouble)
//      val output =
//        s"[$name] more than $allowedFailingTestCount failing tests detected!" + "\n" +
//        s"[$name] $passedCount of $testCount tests passed validation ($passedPercent%)" + "\n" +
//        s"[$name] first 5 failing tests:" + "\n" +
//        failures.take(5).zipWithIndex.map { case ((testResult,failure),i) =>
//          s"[$name-test#${i+1}] result=${testResult.result} sched=${testResult.sched} failure=$failure"
//        }.mkString("\n")
//
//      // Wrap in a TestFailedException for display in sbt
//      failures.head._2 match {
//        case failure:TestFailedException =>
//          throw new TestFailedException(
//            messageFun = { _ => Some(output) },//failure.messageFun,
//            cause = Some(new RuntimeException(output, failure.getCause)),
//            // TODO: figure out how to get proper line number to display
//            failedCodeStackDepthFun = failure.failedCodeStackDepthFun,
//            payload = failure.payload
//          )
//        case failure => throw failure
//      }
//    }
//  }
//
//  /**
//   * Duration validation method
//   */
//  def validateDuration(avgComputedDelay_ns: Long, warmup: DurationResult, result: DurationResult) = { }
//
//  // Used to capture cpu time
//  private[this] val bean = ManagementFactory.getThreadMXBean
//
//  /**
//   * Run test testCount times ensuring:
//   *   1) test completes within maxWait nanoseconds
//   *   2) all Futures generated by test complete within maxWait nanoseconds
//   * */
//  def run() {
//
//    val maxWait = maxWait_ns.nanos
//
//    // Create custom thread factory to allow getting more info about workers threads
//    val threads = ArrayBuffer[Thread]()
//    val threadFactory = new ThreadFactory {
//      override def newThread(r: Runnable) = {
//        val i = threads.size + 1
//        val retv = new Thread(r,s"$name-worker-$i")
//        threads.synchronized { threads += retv }
//        retv
//      }
//    }
//
//    def runTest(executor: ExecutorService, warmup: Boolean, i: Int) : TestResult[R] = {
//
//      implicit val ctc = ConcurrentTestContext(executor)
//      import ctc._
//
//      val test = mkTest()
//      test.onBeforeTest()
//      val result = test.test().getTry(maxWait)
//      test.onAfterTest()
//      val elapsedDuration = timer.elapsedDuration
//      waitForActiveRunnableCount(0)
//
//      if(debugMode > 1) {
//        println(s"elapsedDuration=$elapsedDuration delayErrorAccumulator_ns=${delayError_ns}")
//      }
//      TestResult(
//        result = result,
//        completedFutureCount = completedRunnableCount,
//        delayError_ns = if(warmup) 0 else delayError_ns,
//        elapsedDuration_ns = elapsedDuration.toNanos,
//        sched = sched,
//        threads = {
//          threads
//            .map { thread =>
//              val threadInfo = bean.getThreadInfo(thread.getId)
//              import threadInfo._
//              val cpuTime_ns = bean.getThreadCpuTime(thread.getId)
//              val userTime_ns = bean.getThreadUserTime(thread.getId)
//              if(debugMode > 1) {
//                println(s"threadId=$getThreadId threadName=$getThreadName blockedTime=$getBlockedTime blockedCount=$getBlockedCount waitedTime=$getWaitedTime waitedCount=$getWaitedCount isNative=$isInNative suspended=$isSuspended state=$getThreadState userTime_ns=$userTime_ns cpuTime_ns=$cpuTime_ns")
//              }
//              TestThreadInfo(
//                threadInfo = threadInfo,
//                cpuTime_ns = cpuTime_ns,
//                userTime_ns = userTime_ns
//              )
//            }
//            .toVector
//        }
//      )
//    }
//
//    val executor = Executors.newFixedThreadPool(workerThreadPoolSize, threadFactory)
//
//    // Running test with no delay may cause serialization oddities so don't test the results just time it
//    // Note: Testing shows that the first run typically takes 3 times longer to complete for unknown reasons so it is
//    // discarded here
//    val rawWarmupResults = (0 until warmupTestCount).map(runTest(executor, true,_)).drop(1)
//    // Because wall clock is used for testing here, have to throw out outlier times that aren't close to the mean (due
//    // to spurious processing delays, such as garbage collection, background processing, etc)
//    val warmupResults = filterOutliersBy(rawWarmupResults, { r:TestResult[R] => r.elapsedDuration_ns.toDouble })
//
//    val rawResults = (0 until testCount).map(runTest(executor, false, _))
//    val results = filterOutliersBy(rawResults, { r:TestResult[R] => r.elapsedDuration_ns.toDouble })
//
//    executor.shutdown()
//
//    val validatedResults = rawResults.map { testResult =>
//      (
//        testResult,
//        Try {
//          validateResult(testResult.result)
//          validateSchedule(testResult.sched)
//        }.failed.toOption
//      )
//    }.toVector
//    validateResults(validatedResults)
//
//    val warmupDurationResult = DurationResult(warmupResults)
//    val durationResult = DurationResult(results)
//    if(debugMode > 0) {
//      println(s"warmupDurationResult=$warmupDurationResult")
//      println(s"durationResult=$durationResult")
//    }
//
//    validateDuration(
//      avgComputedDelay_ns = durationResult.avgCpuTime_ns - durationResult.avgDelayErr_ns - warmupDurationResult.avgCpuTime_ns,
//      warmup = warmupDurationResult,
//      result = durationResult
//    )
//  }
//
//}
//
//object ConcurrentTestHarness {
//
//  case class TestThreadInfo(
//    threadInfo: ThreadInfo,
//    cpuTime_ns: Long,
//    userTime_ns: Long
//  )
//  case class TestResult[R](
//    result: Try[R],
//    completedFutureCount: Int,
//    delayError_ns: Long,
//    elapsedDuration_ns: Long,
//    sched: SerializationSchedule[String],
//    threads: Vector[TestThreadInfo]
//  )
//  case class DurationResult(
//    avgDelayErr_ns: Long,
//    avgDuration_ns: Long,
//    avgCpuTime_ns: Long,
//    avgUserTime_ns: Long
//  ) {
//    override def toString = s"DurationResult(avgDelayErr_ns=$avgDelayErr_ns, avgDuration_ns=$avgDuration_ns, avgCpuTime_ns=$avgCpuTime_ns, avgUserTime_ns=$avgUserTime_ns)"
//  }
//  object DurationResult {
//    def apply[R](results: Traversable[TestResult[R]]) : DurationResult = {
//      if(results.nonEmpty) {
//        DurationResult(
//          avgDelayErr_ns = mean(results.map(_.delayError_ns.toDouble)).toLong,
//          avgDuration_ns = mean(results.map(_.elapsedDuration_ns.toDouble)).toLong,
//          avgCpuTime_ns = mean(results.map(_.threads.map(_.cpuTime_ns).sum.toDouble)).toLong,
//          avgUserTime_ns = mean(results.map(_.threads.map(_.userTime_ns).sum.toDouble)).toLong
//        )
//      } else {
//        DurationResult(0,0,0,0)
//      }
//    }
//  }
//
//  def mean(values: TraversableOnce[Double]) = values.sum / values.size
//  def stddev(mean: Double, values: TraversableOnce[Double]) = Math.sqrt(values.map(v => (mean - v) * (mean - v)).sum / values.size)
//  // Not the best method, but for now throw away outliers based on greater than 2 stddevs from mean
//  // Better solution: http://www.itl.nist.gov/div898/handbook/eda/section3/eda35h2.htm
//  def isOutlier(mean: Double, stddev: Double)(v: Double) : Boolean = Math.abs(v - mean) > stddev * 2.0
//
//  import scala.language.higherKinds
//  def filterOutliersBy[T, M[AA] <: TraversableLike[AA,M[AA]]](xa: M[T], f: T => Double)(implicit bf: CanBuildFrom[Nothing, Double, M[Double]]) : M[T] = {
//    val values = xa.map[Double, M[Double]](f)(scala.collection.breakOut)
//    val m = mean(values)
//    val s = stddev(m, values)
//    val outlier = isOutlier(m, s) _
//    val filterFn = { t:T =>  outlier(f(t)) == false }
//    xa.filter(filterFn)
//  }
//
//}