package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part3testing.BasicSpec.SimpleActor

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  // setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "echo, test"
      echoActor ! message

      expectMsg(message) // fails after timeout of 3s  akka.test.single-expect-default
    }
  }

  "A black hole" should {
    "send nothing back" in {
      val blackHole = system.actorOf(Props[BlackHole])
      val message = "hello, test"
      blackHole ! message

      expectNoMessage(1.second)
    }
  }

  "A labtestactor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])

    "turn string uppercase" in {
      labTestActor ! "I like Akka"
      val reply = expectMsgType[String]
      assert(reply == "I LIKE AKKA")
    }

    "react to a greeting" in {
      labTestActor ! "greet"
      expectMsgAnyOf("hi", "hello")
    }

    "reply to two things" in {
      labTestActor ! "two responds"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with two things in a different way" in {
      labTestActor ! "two responds"
      val messages = receiveN(2) // Seq[Any]

      // free to make more complex assertions
    }

    "reply to two things in fancy way" in {
      labTestActor ! "two responds"
      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }

  }


}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case msg => sender() ! msg
    }
  }
  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val rndm = new Random()
    override def receive: Receive = {
      case "greet" =>
        if (rndm.nextBoolean) sender() ! "hello" else sender() ! "hi"
      case "two responds" =>
          sender() ! "Akka"
          sender() ! "Scala"
      case msg:String => sender() ! msg.toUpperCase
    }
  }
}

