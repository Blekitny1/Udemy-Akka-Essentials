package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App{

  // Actors can create other actors

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._

    var child: ActorRef = null

    override def receive: Receive = firstReceive

    def firstReceive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name) // new actor created
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }

  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  import Parent._
  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey kid")

  // actor hierarchies
  // parent - child hierarchy can go as deep and as wide as you want
  // Guardian actors (top-level): /system = system guardian, /user = user-level guardian parents our, / = root guardian

  // actor selection

  val childSelection = system.actorSelection("/user/parent/child") // if leads nowhere gets sent to deadletters
  childSelection ! "I found you"

  // NEVER PASS MUTABLE ACTOR STATE OR THIS REFERENCE TO CHILD ACTORS //



}
