package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object actorsExercises extends App{

  val system = ActorSystem("mySystem")
  class CounterActor extends Actor {
    var number = 0
    override def receive: Receive = {
      case Up(i) =>
        println(s"I - $self am incrementing by $i")
        number += i
      case Down(i) =>
        println(s"I - $self am decrementing by $i")
        number -= i
      case Print() => println(s"I - $self hold a number $number")
    }
  }

  case class Up(i: Int)
  case class Down(i: Int)
  case class Print()

  val counterActor = system.actorOf(Props[CounterActor], "counter1")
  counterActor ! Up(3)
  counterActor ! Down(5)
  counterActor ! Print()

  class BankActor extends Actor {
    var amount: Int = 20
    override def receive: Receive = {
      case Deposit(i) =>
        println(s"I $self deposit $i")
        amount += i
      case Withdraw(i) =>
        println(s"I $self withdraw $i")
        amount -= i
      case Statement() => println(s"I $self hold $amount")
      case _ => println("Illegal operation!")
    }
  }

  case class Deposit(i: Int)
  case class Withdraw(i: Int)
  case class Statement()

  val bankActor = system.actorOf(Props[BankActor], "santander")
  bankActor ! Deposit(5)
  bankActor ! Withdraw(10)
  bankActor ! Statement()
  bankActor ! Withdraw(50)

}
