package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.actorsExercisesSolutions.Person.Live

object actorsExercisesSolutions extends App {

  // Domain of the counter
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  val sys = ActorSystem("sys")
  import Counter._
  val counter = sys.actorOf(Props[Counter], "myCounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 8).foreach(_ => counter ! Decrement)
  counter ! Print

  // bank account
  object BankAcc {
    case class Deposit(i: Int)
    case class Withdraw(i: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
    }

  class BankAcc extends Actor {
    import BankAcc._
    var funds = 0

    override def receive: Receive = {
      case Deposit(i) =>
        if (i < 0) sender() ! TransactionFailure("Invalid deposit amount")
        else {
          funds += i
          sender() ! TransactionSuccess(s"Successfully deposited $i")
        }
      case Withdraw(i) =>
        if (i < 0) sender() ! TransactionFailure("Invalid withdraw amount")
        else if (i > funds) sender() ! TransactionFailure("Insufficient funds")
        else {
          funds -= i
          sender() ! TransactionSuccess(s"Successfully withdrew $i")
        }
      case Statement => sender() ! s"Your balance is $funds"
        }
    }

  object Person {
    case class Live(acc: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAcc._

    override def receive: Receive = {
      case Live(acc) =>
        acc ! Deposit (1000)
        acc ! Withdraw (4444)
        acc ! Withdraw (444)
        acc ! Statement
      case message => println(message.toString)
    }
  }

  val account = sys.actorOf(Props[BankAcc], "bankAcc")
  val person = sys.actorOf(Props[Person], name = "Bob")
  person ! Live(account)
}
