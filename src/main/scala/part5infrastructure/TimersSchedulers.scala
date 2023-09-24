package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.duration._

object TimersSchedulers extends App{

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder fo simpleActor")

  // override implicit val executionContext = system.dispatcher
  import system.dispatcher // we prefer this

  system.scheduler.scheduleOnce(1 second) {
    simpleActor ! "reminder"
  } // (system.dispatcher) // role of execution context

  val routine: Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "heartbeat" // repeated schedule
  }

  system.scheduler.scheduleOnce(5 seconds) {
    routine.cancel() // type cancellable can be cancelled
  }

  /**
   * Implement a self closing actor
   * -if an actor receive a message, you have 1 second to send another
   * -if not, the time window expires and the actor with stop itself
   * -if you send another message, the time window resets
   */





}
