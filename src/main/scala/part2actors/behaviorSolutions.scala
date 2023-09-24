package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.behaviorExercises.sys

import scala.collection.mutable

object behaviorSolutions extends App{

  val sys = ActorSystem("sys")
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] My current count is $currentCount")
    }

  }

  import Counter._

  val counter = sys.actorOf(Props[Counter], "myCounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 8).foreach(_ => counter ! Decrement)
  counter ! Print

  ///////////////////////////////////////////////////////////////////////////////////

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {

    override def receive: Receive = {
      case Vote(c) =>
        println("I am voting!")
        context.become(voted(Some(c)))
      case VoteStatusRequest =>
        println("I did not vote")
        sender() ! VoteStatusReply(None)
    }

    def voted(candidate: Option[String]): Receive = {
      case VoteStatusRequest =>
        sender() ! VoteStatusReply(candidate)
        println("I have voted")

    }
  }

  case class AggragateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {

    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive ={
      case AggragateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive ={
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggragator] Poll stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }

  }

  val alice = sys.actorOf(Props[Citizen])
  val bob = sys.actorOf(Props[Citizen])
  val charlie = sys.actorOf(Props[Citizen])
  val daniel = sys.actorOf(Props[Citizen])

  val voteAgg = sys.actorOf(Props[VoteAggregator])

  alice ! Vote("Martin")
  bob ! Vote("Ivan")
  charlie ! Vote("Martin")
  daniel ! Vote("Marek")

  voteAgg ! AggragateVotes(Set(alice, bob, charlie, daniel))

}
