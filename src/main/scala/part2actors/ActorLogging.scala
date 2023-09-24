package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App{

  class WithLogger extends Actor {
  // #1 explicit logging
    val logger = Logging(context.system, this)
    /*
    0 - DEBUG
    1 - INFO
    2 - WARN
    3 - ERROR
     */
    override def receive: Receive = {
      case message => logger.info(message.toString) // log it
    }
  }

  val system = ActorSystem("LogDemo")
  val actor = system.actorOf(Props[WithLogger])
  actor ! "Logging a message!"

  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things: {} and {}", a, b) // interpolate a, b
      case message => log.info(message.toString)
    }
  }

  val actor2 = system.actorOf(Props[ActorWithLogging])
  actor2 ! "Logging with a trait"
  actor2 ! (23, 21)

}


