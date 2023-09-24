package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.util.Random

object Dispatchers extends App{

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case msg =>
        count += 1
        log.info(s"[$count] $msg")
    }
  }

  // 1 - incode attaching

  val system = ActorSystem("DispatchersDemo")//, ConfigFactory.load().getConfig("dispatcherDemo"))
//  val actors = for(i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//  val r = new Random()
//  for (i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }

  // 2 - from config

  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   * Dispatcher implement the ExecutionContext trait
   */

  import scala.concurrent.Future

  class DBActor extends Actor with ActorLogging {

    // solution 1 to hiccups, solution 2 is router actor!
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")
    override def receive: Receive = {
      case msg => Future {
        // wait on a resource
        Thread.sleep(5000)
        log.info(s"Success: $msg")
      }
    }
  }

  val dbActor = system.actorOf(Props[DBActor])
  // dbActor ! 15

  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val msg = s"important message $i"
    dbActor ! msg
    nonBlockingActor ! msg // Thread.sleep creates a 5s hiccup for messages the actors
  }


}
