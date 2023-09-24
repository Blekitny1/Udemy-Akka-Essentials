package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorsExercises.WordCounterMaster.{GetTask, ReportTotalCount}

object ChildActorsExercises extends App {

  // Distributed word counting

  val system = ActorSystem("sys")

  object WordCounterMaster {

    def props(name: String): Props = Props(new WordCounterMaster(name))

    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
    case class GetTask(text: String)
    case object ReportTotalCount
  }

  class WordCounterMaster(name: String) extends Actor {
    import WordCounterMaster._

    override def receive: Receive = firstReceive

    def firstReceive: Receive = {
      case Initialize(nChildren: Int) =>
          (0 until nChildren).foreach(
        i => context.actorOf(WordCounterWorker.props(workerNr = i), s"Worker$i")
        )
        context.become(nextReceive(0, nChildren, 0))
    }

    def nextReceive(nextWorkerNr: Int, nrOfWorkers: Int, currentCount: Int): Receive = {
      case GetTask(text: String) =>
        system.actorSelection(s"/user/me/Worker" + nextWorkerNr) ! WordCountTask(text)
        context.become(nextReceive((nextWorkerNr + 1) % nrOfWorkers, nrOfWorkers, currentCount))
      case WordCountReply(count: Int) =>
        context.become(nextReceive(nextWorkerNr, nrOfWorkers, currentCount + count))
      case ReportTotalCount => println(s"Total count is $currentCount")


    }
  }

  object WordCounterWorker {
    def props(workerNr: Int): Props = Props(new WordCounterWorker(workerNr))
  }

  class WordCounterWorker(workerNr: Int) extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(text: String) => sender() ! WordCountReply(text.split(" ").length)
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
  import WordCounterWorker._

  val master = system.actorOf(WordCounterMaster.props("me"))
  master ! Initialize(3)
  master ! GetTask("wer wqer qqwer")
  master ! GetTask("wer wqer")
  master ! GetTask("wer wqer qqwer")
  master ! GetTask("wer qqwer")
  master ! GetTask("wer wqer qqwer")
  master ! ReportTotalCount


}
