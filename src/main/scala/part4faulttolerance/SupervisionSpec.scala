package part4faulttolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part4faulttolerance.SupervisionSpec.AllForOneSupervisor

class SupervisionSpec extends TestKit(ActorSystem("SupervisionSpec"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A supervisor" should {
    "resume its child in case of minor fault" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I like Akka"
      child ! Report
      expectMsg(3)

      child ! "q q q q q q q q q q q q q q q q q q q q q q q q"
      child ! Report // causes Resume which keeps internal state
      expectMsg(3)
    }

    "restart its child in case of empty sentence" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I like Akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report // restarts and clears the internal state
      expectMsg(0)
    }

    "terminate its child in case of major error" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      watch(child) // register for death watch

      child ! "akka is nice"
      child ! Report // terminates a child thus we expect a terminated message
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)
    }

    "escalate an error when it does not know what to do" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[Supervisor], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      watch(child)

      child ! 11
      child ! Report
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)

    }
  }

  "A kinder supervisor" should {
    "not kill children in case it's restarted or escalates" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[NoDeathOnRestart], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "Akka is cool"
      child ! Report
      expectMsg(3)

      child ! 12
      child ! Report
      expectMsg(0)
    }
  }

  "An AllForOne supervisior" should {
    "apply all for one strategy" in {
      import SupervisionSpec._
      val supervisor = system.actorOf(Props[AllForOneSupervisor], "allForOneSupervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      supervisor ! Props[FussyWordCounter]
      val secondChild = expectMsgType[ActorRef]

      secondChild ! "Testing supervision"
      secondChild ! Report
      expectMsg(2)

      EventFilter[NullPointerException]() intercept {
        child ! ""
      }

      Thread.sleep(500)
      secondChild ! Report
      expectMsg(0)

    }
  }

}

object SupervisionSpec {

  class Supervisor extends Actor {

    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  class NoDeathOnRestart extends Supervisor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {

    }
  }

  class AllForOneSupervisor extends Supervisor {
    override val supervisorStrategy = AllForOneStrategy() { // all children are affected
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }
  }

  case object Report
  class FussyWordCounter extends Actor {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        if (sentence.length > 20) throw new RuntimeException("sentence is too big")
        else if (!Character.isUpperCase(sentence(0))) throw new IllegalArgumentException("sentence must be upper case")
        else words += sentence.split(" ").length
      case _ => throw new Exception("can only receive strings")
    }
  }

}
