package part6stashes

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object demo extends App {

  /*
  ResourceActor
  - open => it can receive r/w request to the resource
  - otherwise postpone these until open
  When closed:
  - Read, Write are POSTPONED
  - Open => switch to open state
  When opened:
  -Read, Write are handled
  - Close => back to closed state

  [Read, Open, Write]
  -stash Read
  -open => switch to Open
  -mailbox: [Read]
  -mailbox: [Write]
  -Read and Write are handled

   */

  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  // 1 - stash trait mixed in last, so "super" works correctly
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        // 3 - unstash all when switching message handler
        unstashAll()
        context.become(open)
      case msg =>
        log.info(s"Stashing $msg because I cant handle it in the closed state")
        // 2 - stash away what you cant handle
        stash()
    }

    def open: Receive = {
      case Read =>
        // pretend to do something
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I am writing $data")
        innerData = data
      case Close =>
        log.info("Closing resource")
        unstashAll()
        context.become(closed)
      case msg =>
        log.info(s"Stashing $msg because I cant handle it in the open state")
        stash()

    }

  }

  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])
  resourceActor ! Read // stashed
  resourceActor ! Open // switched to open, popped Read
  resourceActor ! Open // stashed
  resourceActor ! Write("I love stash") // done
  resourceActor ! Close // closed, popped open
  resourceActor ! Read // done

}
