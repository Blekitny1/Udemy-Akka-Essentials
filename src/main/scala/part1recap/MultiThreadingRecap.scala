package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultiThreadingRecap {

  // creating threads

  val aThread = new Thread(() => println("I run!"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()

  // different runs produce different results

  class BankAcc(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int): Unit = this.amount -= money

    def safeWithdraw(money: Int):Unit = this.synchronized {
      this.amount -= money
    }
  }

  // Scala Futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation on different thread
    42
  }

  // callbacks
  future.onComplete {
    case Success(42) => println("I found!")
    case Failure(_) => println("Not found!")
  }

  val aProcessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture = future.flatMap{
    value => Future(value + 2)
  } // Future with 44

  val filteredFuture = future.filter(_ % 2 == 0) // if no element passes => NoSuchElementException









}
