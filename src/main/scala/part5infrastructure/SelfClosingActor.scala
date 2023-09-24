package part5infrastructure
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._


object SelfClosingActor extends App{

  val system = ActorSystem("selfStopping")

  class SelfStoppingActor extends Actor with ActorLogging {
    override def receive: Receive = firstReceive

  def firstReceive: Receive = {
    case msg =>
      log.info(msg.toString)
      import system.dispatcher
      context.system.scheduler.scheduleOnce(1 seconds) {
        context.stop(self)
      }
      context.become(firstReceive)
  }

  def waitForMessageForSecondReceive: Receive = {
    case msg =>
      log.info(msg.toString)
      context.become(waitForMessageForSecondReceive)
  }

  }

//  val selfStoppingActor = system.actorOf(Props[SelfStoppingActor])
//  selfStoppingActor ! "first"
//  Thread.sleep(500)
//  selfStoppingActor ! "second"
//  Thread.sleep(1500)
//  selfStoppingActor ! "third"

  /////////////////////////////// SOLUTION ////////////////////////////////////

  class SelfClosingActor extends Actor with ActorLogging {
    var schedule = createTimeoutWindow()

    import system.dispatcher

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }

    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case msg =>
        log.info(s"Received $msg, staying alive")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }


  }

//  val selfStoppingActor = system.actorOf(Props[SelfClosingActor])
//  selfStoppingActor ! "first"
//  Thread.sleep(500)
//  selfStoppingActor ! "second"
//  Thread.sleep(1500)
//  selfStoppingActor ! "third"

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop

  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("hello")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second) // the previous timer of that key is cancelled
      case Reminder => log.info("I am alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)

    }

  }

  import system.dispatcher
  val timerBasedHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor], "me")
  system.scheduler.scheduleOnce(5 seconds){
    timerBasedHeartbeatActor ! Stop
  }

}
