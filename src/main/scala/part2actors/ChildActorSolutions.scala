package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorSolutions extends App {

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = firstReceive

    def firstReceive: Receive = {
      case Initialize(nChildren: Int) =>
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(nextReceive(childrenRefs, 0, 0, Map()))
    }

    def nextReceive(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: $text - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(nextReceive(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with count $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(nextReceive(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))

    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text: String) =>
        println(s"${self.path} I have received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)

    }
  }

  /**
   * create WordCountMaster
   * send initialize(10) to Master
   * send "asd sdf dfg" to Master
   * Master sends a Task to one of its children
   * Child replies with Reply (3)
   * Master replies with (3) to sender
   *
   * requester -> master -> worker -> master -> requester
   *
   * round robin logic 1.2.3.4.5.1.2
   */

  import WordCounterMaster._

  class TestActor extends Actor {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        master ! "wer wqer qqwer"
        master ! "wer wqer"
        master ! "wer wqer qqwer"
        master ! "wer qqwer"
        master ! "wer wqer qqwer"
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("woo")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

}
