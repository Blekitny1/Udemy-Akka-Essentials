package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.actorsExercisesSolutions.Counter

import scala.collection.mutable

object behaviorExercises extends App{
  val sys = ActorSystem("sys")
  /*object Counter {
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
  counter ! Print*/

///////////////////////////////////////////////////////////////////////////
  

  class Citizen extends Actor {
    import Citizen._
    override def receive: Receive = notVotedReceive

    def notVotedReceive: Receive = {
      case Vote(s: String) =>
        println("I am voting!")
        context.become(votedReceive(s))
      case VoteStatusRequest =>
        println("I did not vote")
        sender() ! VoteStatusReply(None)
    }

    def votedReceive(s: String) : Receive = {
      case Vote(_) =>
        println("I cant vote, I have already voted")
      case VoteStatusRequest =>
        sender() ! VoteStatusReply(Some(s))
        println("I have voted")

    }
  }

  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate: Option[String])
  }

  object VoteAggregator {
    case class AggragateVotes(citizens: Set[ActorRef])
    case object PrintResults
  }

  class VoteAggregator extends Actor {
    import VoteAggregator._
    import Citizen._

    var resultsMap: mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)

    override def receive: Receive = {
      case AggragateVotes(citizens: Set[ActorRef]) =>
        citizens.foreach(voter => voter ! VoteStatusRequest)
      case VoteStatusReply(Some(s)) =>
        if (!resultsMap.contains(s)) {
          resultsMap(s) = 1
        } else {
          resultsMap(s) += 1
        }
      case VoteStatusReply(None) =>
      case PrintResults =>
        println("ER")
        resultsMap.keys.foreach{case (k: String) => println(k, resultsMap(k))}

    }
  }

  import Citizen._
  import VoteAggregator._

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
  voteAgg ! PrintResults

  /*
  Print:
  Martin -> 2, Ivan -> 1, Marek -> 1
   */
}
