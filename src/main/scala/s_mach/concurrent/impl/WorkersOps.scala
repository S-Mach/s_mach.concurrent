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


import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import s_mach.concurrent._
import s_mach.concurrent.util._

object WorkersOps {

  /**
   * Transform Futures concurrently, limiting concurrency to at most
   * WorkerConfig.workerCount workers
   * @return a Future of M[B] that completes once all Futures have been
   *         transformed
   */
  def mapWorkers[A,B,M[+AA] <: TraversableOnce[AA]](workerCount: Int)(
    self: M[A],
    f: A => Future[B]
  )(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {
    val builder = cbf()
    val lock = Lock()
    val output = { b:B =>
      lock.lock {
        builder += b
        Future.unit
      }
    }
    runWorkers(workerCount)(self, f, output).map { _ => builder.result() }
  }

  /**
   * Transform and flatten Futures concurrently, limiting concurrency to at
   * most WorkerConfig.workerCount workers
   * @return a Future of M[B] that completes once all Futures have been
   *         transformed
   */
  def flatMapWorkers[A,B,M[+AA] <: TraversableOnce[AA]](workerCount: Int)(
    self: M[A],
    f: A => Future[TraversableOnce[B]]
  )(implicit
    cbf: CanBuildFrom[Nothing, B, M[B]],
    ec: ExecutionContext
  ) : Future[M[B]] = {

    val builder = cbf()
    val lock = Lock()
    val output = { b:TraversableOnce[B] =>
      lock.lock {
        builder ++= b
        Future.unit
      }
    }
    runWorkers(workerCount)(self, f, output).map { _ => builder.result() }
  }

  /**
   * Traverse Futures concurrently, limiting concurrency to at most
   * WorkerConfig.workerCount workers
   * @return a Future of M[B] that completes once all Futures have been
   *         transformed
   */
  def foreachWorkers[A,U,M[+AA] <: TraversableOnce[AA]](workerCount: Int)(
    self: M[A],
    f: A => Future[U]
  )(implicit ec:ExecutionContext) : Future[Unit] = {

    val output = { _:U => Future.unit }
    runWorkers(workerCount)(self, f, output).map { _ => () }
  }

  /**
   * Using the collection members xa, concurrently execute a processing
   * function f, outputting the result of f to the function g. Concurrency is
   * limited to the number of workers specified in WorkersConfig. Any exception
   * that occurs during a worker's processing will cause a AsyncParThrowable
   * to be immediately returned. Any other exceptions from workers that have yet
   * to complete can be retrieved from AsyncParThrowable.
   *
   * @param xa the collection
   * @param f the processing function
   * @param g the output function
   * @param workerCount specifies the number of workers
   * @tparam A the type processed
   * @tparam B the output type
   * @return a Future that completes once all workers have completed.
   */
  def runWorkers[A,B](workerCount: Int)(
    xa: TraversableOnce[A],
    f: A => Future[B],
    g: B => Future[Unit]
  )(implicit ec:ExecutionContext) : Future[Unit] = {

    val s = Semaphore(workerCount)
    val o = Sequencer()
    val workerFailures = ListQueue[Throwable]()
    def mkWorkerFailureException() = {
      workerFailures.poll().flatMap { head =>
        Future.failed {
          val tail = workerFailures.poll(workerFailures.offerQueueSize)
           AsyncParThrowable(head, tail)
        }
      }
    }

    for {
      i <- {
        xa.async.foldLeft(0) { (i,a) =>
          if(workerFailures.offerQueueSize == 0) {
            s.acquire(1) {
              // Run worker in the background
              f(a).toTry
                // Worker returns Future[Try[]] to ensure that exceptions from
                // f are not carried in the future but in the result. There
                // should be no exceptions in Future here (unless there is a bug
                // here in WorkerOps)
                .flatMap {
                  // Worker completed successfully
                  case Success(b) =>
                    // Ensuring proper sequence of results
                    o.when(i)(g(b))
                  case Failure(t) =>
                    // Worker failed - start process of early exit
                    workerFailures.offer(t)
                    // Still need to ensure sequence can progress to allow later
                    // workers to finish
                    o.when(i)(Future.unit)
                }
            }.deferred.map { inner =>
              // Throwaway result of worker but make sure to at least report
              // exceptions to ExecutionContext
              inner.background
              i + 1
            }
          } else {
            // Detected a failure. Wait for workers to complete then end loop
            // early with failures
            s.acquire(workerCount) { mkWorkerFailureException() }
          }
        }
      }
      // Wait for completion
      _ <- s.acquire(workerCount)(Future.unit)
      // If an exception occurs during the last worker it won't be detected
      // during the main loop
      _ <- if(workerFailures.offerQueueSize == 0) {
        Future.unit
      } else {
        mkWorkerFailureException()
      }
    } yield ()
  }
}
