import cats.effect.kernel.Outcome
import cats.effect.{Fiber, FiberIO, IO, IOApp}

import scala.concurrent.duration.DurationInt

object Start extends IOApp.Simple {

  val age: IO[Int] = IO(26)
  val age2 : IO[Int] = IO(52)

  implicit class SomeClass[A](io: IO[A]) {
    def debug: IO[A] = io.map { value =>
      println(s"[${Thread.currentThread.getName}] $value")
      value
    }
  }

  val aFiber: IO[Fiber[IO, Throwable, Int]] = new SomeClass[Int](age).debug.start

  val theSameThread = for {
    _ <- new SomeClass[Int](age).debug
    _ <- new SomeClass[Int](age2).debug
    _ <- new SomeClass[Fiber[IO, Throwable, Int]](aFiber).debug
  } yield ()

  def runOnAnotherThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = for {
    fib <- io.start
    value <- fib.join
  } yield value

  //  new SomeClass[Outcome[IO, Throwable, Int]](runOnAnotherThread[Int](age)).debug.void
  def throwTheadError(): IO[Outcome[IO, Throwable, Int]] = for {
    fib <- IO.raiseError[Int](throw new Exception("No Element Found!")).start
    result <- fib.join
  } yield result

  def cancelTest(): IO[Outcome[IO, Throwable, String]] = {
    val task = new SomeClass[String](IO("starting")).debug *> IO.sleep(1.second) *> new SomeClass[String](IO("done")).debug
    for {
      fib <- task.start
      _ <- IO.sleep(500.millis) *> IO("Cancelling").map { value =>
        println(s"[${Thread.currentThread.getName}] $value")
        value
      }
      _ <- fib.cancel
      result <- fib.join
    } yield result
  }
  //  new SomeClass[Outcome[IO, Throwable, String]](cancelTest()).debug.void
  /*
    1 - success(IO(value))
    2 - errored(ex)
    3 - cancelled
   */

  val firstIO = IO("firstIO starting") *> IO.sleep(1000.millis) *> IO("Done first IO") *> IO(26)
  val cancelResult = firstIO.onCancel(IO("task one is canceled"))
  val secondIO = IO("secondIO starting") *> IO.sleep(500.millis) *> IO("Done second IO")

  def testRace(firstIO: IO[Int], secondIO: IO[String]): IO[String] = {
    val raceResult: IO[Either[Int, String]] = IO.race(firstIO, secondIO) // Either[A,B]
    raceResult.flatMap {
      case Right(value) => IO(s"Second won! $value")
      case Left(value) => IO(s"First won! $value")
    }
  }

  // timeout

  val cancelIt = cancelResult.timeout(500.millis)

  //race
  def raceFibers[A](firstIO: IO[A], secondIO: IO[A]) ={
    val pair = IO.racePair(firstIO, secondIO) //IO[Either[(OutcomeIO[A], FiberIO[B]), (FiberIO[A], OutcomeIO[B])]]
    pair
  }


  override def run: IO[Unit] = new SomeClass[String](testRace(cancelResult, secondIO)).debug.void
}
