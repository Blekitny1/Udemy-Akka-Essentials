package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "hi!" => sender() ! "Hello, there!" // replying to a message
      case message: String => println(s"[${self}] I have received $message")
      case int: Int => println(s"[simple actor] I have received a NUMBER $int")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something special: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep original sender
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "Hello, actor"

  // 1 - messages can be of any type; must be immutable and serializable
  // in practice we use case classes and case objects
  simpleActor ! 41

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("special content")

  // 2 - actors have information about their context and themselves
  // context.self <==> .self <==> this in OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am happy!")

  // 3 - actor can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters [INFO] prompt
  alice ! "hi!" // reply to "me"

  // 5 - forwarding messages

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("hi!", bob) // the sender is Alice and Bob responds

  /**
   * 1. a counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   *  2. a bank account actor
   *  - deposit amount - reply with success/failure
   *  - withdraw amount - reply with success/failure
   *  - statement
   *  - test by other kind of actor
   */
}
