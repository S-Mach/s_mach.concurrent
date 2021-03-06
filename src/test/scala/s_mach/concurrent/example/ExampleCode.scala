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
package s_mach.concurrent.example

import scala.util.Try


/**
  * Example code in readme -- not used in testing but placed here for compile checks
  */
object ExampleCode {
  import scala.concurrent._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import s_mach.concurrent._
  import java.net.SocketTimeoutException


  implicit val scheduledExecutionContext = ScheduledExecutionContext(2)
  case class Item(id: String, value: Int, relatedItemId: String)
  def read(id: String) : Future[Item] = Future { Thread.sleep(1000); println(id); Item(id,id.toInt,(id.toInt+1).toString) }
  def readFail(id: String) : Future[Item] = Future { Thread.sleep(1000); println(id); throw new RuntimeException(id.toString) }
  def longRead(id: String) : Future[Item] = Future { Thread.sleep(2000); println(id); Item(id,id.toInt,(id.toInt+1).toString) }
  def write(id: String, item: Item) : Future[Boolean] = Future { Thread.sleep(1000); println(id); true }
  def writeFail(id: String, item: Item) : Future[Boolean] = Future { Thread.sleep(1000); println(id); throw new RuntimeException(id.toString) }

  def example1: Future[Boolean] = {
    val oomItemIdBatch = (1 to 10).toList.map(_.toString).grouped(2).toList
    val future = { // necessary for pasting into repl
      for {
        oomItem <- {
          println("Reading...")
          oomItemIdBatch
            // Serially perform read of each batch
            .foldLeft(Future.successful(List[Item]())) { (facc, idBatch) =>
              for {
                acc <- facc
                // Concurrently read batch
                oomItem <- Future.sequence(idBatch.map(read))
              } yield acc ::: oomItem
            }
        }
        _ = println("Computing...")
        oomNewItemBatch = oomItem.map(item => item.copy(value = item.value + 1)).grouped(2).toList
        oomResult <- {
          println("Writing...")
          oomNewItemBatch
            // Serially perform write of each batch
            .foldLeft(Future.successful(List[Boolean]())) { (facc, itemBatch) =>
              for {
                acc <- facc
                // Concurrently write batch
                oomResult <- Future.sequence(itemBatch.map(item => write(item.id, item)))
              } yield acc ::: oomResult
            }
        }
      } yield oomResult.forall(_ == true)
    }
    future
  }

  def example2: Future[Boolean] = {
    val oomItemIdBatch = (1 to 10).toList.map(_.toString).grouped(2).toList
    val future = { // necessary for pasting into repl
      for {
        oomItem <- {
          println("Reading...")
          oomItemIdBatch.async.flatMap(_.async.par.map(read))
        }
        _ = println("Computing...")
        oomNewItemBatch = oomItem.map(item => item.copy(value = item.value + 1)).grouped(10).toVector
        oomResult <- {
          println("Writing...")
          oomNewItemBatch.async.flatMap(_.async.par.map(item => write(item.id, item)))
        }
      } yield oomResult.forall(_ == true)
    }
    future
  }

  def example3: Future[Boolean] = {
    val oomItemIdBatch = (1 to 10).toList.map(_.toString).grouped(2).toList
    val future = { // necessary for pasting into repl
      for {
        oomItem <- {
          println("Reading...")
          oomItemIdBatch.async.par(2).flatMap(_.async.par(4).map(read))
        }
        _ = println("Computing...")
        oomNewItemBatch = oomItem.map(item => item.copy(value = item.value + 1)).grouped(10).toVector
        oomResult <- {
          println("Writing...")
          oomNewItemBatch.async.par(2).flatMap(_.async.par(4).map(item => write(item.id, item)))
        }
      } yield oomResult.forall(_ == true)
    }
    future
  }

  def example4: Future[Boolean] = {
    val oomItemIdBatch = (1 to 10).toList.map(_.toString).grouped(2).toList
    val future = { // necessary for pasting into repl
      for {
        oomItem <- {
          println("Reading...")
          oomItemIdBatch
            .async
            .progress(1.second)(progress => println(progress))
            .throttle(3.seconds)
            .flatMap { batch =>
              batch
                .async.par
                // Retry at most first 3 timeout and socket exceptions after delaying 100 milliseconds
                .retry {
                  case (_: TimeoutException) :: tail if tail.size < 3 =>
                    Future.delayed(100.millis)(true)
                  case (_: java.net.SocketTimeoutException) :: tail if tail.size < 3 =>
                    Future.delayed(100.millis)(true)
                  case _ => false.future
                }
                .map(read)
            }
        }
        _ = println("Computing...")
        oomNewItemBatch = oomItem.map(item => item.copy(value = item.value + 1)).grouped(10).toVector
        oomResult <- {
          println("Writing...")
          oomNewItemBatch.async.par(2).flatMap(_.async.par(4).map(item => write(item.id, item)))
        }
      } yield oomResult.forall(_ == true)
    }
    future
  }

  def example5: Future[(Item,Item,Item)] ={
    for {
      i1 <- read("1")
      i2 <- read("2")
      i3 <- read("3")
    } yield (i1,i2,i3)
  }

  def example6: Future[(Item,Item,Item)] = {
    val f1 = read("1")
    val f2 = read("2")
    val f3 = read("3")
    val future = { // necessary for pasting into repl
      for {
        i1 <- f1
        i2 <- f2
        i3 <- f3
      } yield (i1,i2,i3)
    }
    future
  }

  def example7: Future[(Item,Item,Item)] = {
//    for {
//      (i1,i2,i3) <- concurrently(read("1"), read("2"), read("3"))
//    } yield (i1,i2,i3)
    for {

      // Note: this requires an execution plan object that supports map/flatMap
//        (i1,i2) <-
//          lazyeval(read("1"), read("2"))
//            .async
//            .par(2)
//            .progress(1.second)(progress => println(progress))
//            .throttle(3.seconds)

      // Not a big fan of this style
//        (i1,i2) <-
//          async(read("1"), read("2"))
//            .par(2)
//            .progress(1.second)(progress => println(progress))
//            .throttle(3.seconds)

        // This method only requires adding a run pimp-my-lib method for 21 run methods to AsyncConfig
        // To avoid having to repeat
        (i1,i2,i3) <-
          async
            .par(2)
            .progress(1.second)(progress => println(progress))
            .throttle(3.seconds)
            .run(read("1"), read("2"), read("3"))

    } yield (i1,i2,i3)
  }

  def example8: Future[(Item,Item,Item)] = {
    for {
      (i1,i2,i3) <-
        async
          .par(2)
          .progress(1.second)(progress => println(progress))
          .retry {
            case (_: TimeoutException) :: tail if tail.size < 3 =>
              Future.delayed(100.millis)(true)
            case (_: SocketTimeoutException) :: tail if tail.size < 3 =>
              Future.delayed(100.millis)(true)
            case _ => false.future
          }
          .run(
            read("1"),
            read("2"),
            read("3")
          )
    } yield (i1,i2,i3)
  }
  def example9: Try[Vector[Item]] = {
    Future.sequence(Vector(longRead("1"),readFail("2"),readFail("3"),read("4"))).awaitTry
  }

  def example10: Try[Vector[Item]] = {
    Vector(longRead("1"),readFail("2"),readFail("3"),read("4")).merge.awaitTry
  }
}

