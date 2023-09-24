package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}
import part4faulttolerance.StartingStoppingActors.Parent.StartChild

object StartingStoppingActors extends App{

  val system = ActorSystem("StoppingActorsDemo")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child with the name $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
      case msg => log.info(msg.toString)


    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - context.stop()
   */

  import Parent._
//  val parent = system.actorOf(Props[Parent], "parent")
//  parent ! StartChild("child1")
//  val child = system.actorSelection("/user/parent/child1")
//  child ! "hi child!"
//  parent ! StopChild("child1")
//  // for (_ <- 1 to 50) child ! "are you still there?" // asynchronousness!
//  parent ! StartChild("child2")
//  val child2 = system.actorSelection("/user/parent/child2")
//  child2 ! "hi child 2"
//  parent ! Stop
//  for (i <- 1 to 10) parent ! s"[${i}] parent, are you there?"
//  // does not occur, since parent stopping must arrive earlier
//  for (j <- 1 to 100) child2 ! s"[${j}] child2, are you there?"
//  // actually stops children first recursively and asynchronously

  /**
   * 2 - using special messages
   */

//  val looseActor = system.actorOf(Props[Child])
//  looseActor ! "hello, looseActor"
//  looseActor ! PoisonPill // you cannot hold it in Receive handler
//  looseActor ! "looseActor, hello?"
//
//  val abrubtlyTerminatedActor = system.actorOf(Props[Child])
//  abrubtlyTerminatedActor ! "you are about to be terminated"
//  abrubtlyTerminatedActor ! Kill // throw ActorKilledExepction
//  abrubtlyTerminatedActor ! "you have been terminated"

  /**
   * Death watch o.o
   */

  class Watcher extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Starting and watching $name")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"The reference that I'm watching $ref has been stopped")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  watchedChild ! "hello, watchedChild"
  Thread.sleep(500)
  watchedChild ! PoisonPill


}
