package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App{

  object FussyKid {
    case object KidAccept
    case object KidReject
    val happy = "happy"
    val sad = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state = happy

    override def receive: Receive = {
      case Food(vege) => state = sad
      case Food(choco) => state = happy
      case Ask(_) =>
        if (state == happy) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(`v`) => context.become(sadReceive, false) // change to sad receive and stack
      case Food(`c`) => // change nothing
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(`v`) => context.become(sadReceive, false) // stack sad
      case Food(`c`) => context.unbecome() // pop from stack
      case Ask(_) => sender() ! KidReject
    }

  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String) // do you want to play
    val v: String = "veggies"
    val c: String = "chocolate"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test interaction
        kidRef ! Food(v)
        kidRef ! Food(v)
        kidRef ! Food(c)
        kidRef ! Food(c)
        kidRef ! Ask("Do you want to play?")

      case KidAccept => println("Yay, lets play!")
      case KidReject => println("At least he is healthy.")

    }
  }

  import Mom._
  import FussyKid._

  val system = ActorSystem("changingActorBehavior")
  val fussyKid = system.actorOf(Props[FussyKid])
  val mom = system.actorOf(Props[Mom])

  // mom ! MomStart(fussyKid)

  /**
   * mom receives MomStart
   * kid receieves Food(vege)
   * kid state changes to sad
   * mom sends Ask(_)
   * kid responds with KidReject
   */

  val statelessKid = system.actorOf(Props[StatelessKid])
  mom ! MomStart(statelessKid)

  /**
   * mom receives MomStart
   * kid receives Food(vege)
   * kid changes to sadReceive
   * mom sends Ask(_)
   * kid responds with KidReject
   */

  /**
   * context.become
   * Food(veg) -> stack.push(sadReceive)
   * Food(choco) -> stack.push(happyReceive)
   * Stack: Happy>Sad>Happy
   */

  /**
   * context.become and context.unbecome
   * Food(veg) -> stack.push(sadReceive) (Sad,Happy)
   * Food(veg) -> stack.push(sadReceive) (Sad,Sad,Happy)
   * Food(choco) -> stack.pop() (Sad, Happy)
   * Food(choco) -> stack.pop() (Happy)
   */

  /**
   * 1 - recreate counter actor with context become and no mutable state
   * 2 - simplified voting system -
   * class Citizen extends Actor {}
   * class VoteAgg extends Actor {}
   * case class Vote(candidiate)
   * case class AggVotes (citizens: Set[ActorRef])
   * case object VoteStatusRequest
   * case object VoteStatusReply(candidate: Option[String])
   */

}

