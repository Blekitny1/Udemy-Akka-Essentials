package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part3testing.TimedAssertionsSpec.{WorkResult, WorkerActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends TestKit(ActorSystem("TestProbeSpec", ConfigFactory.load().getConfig("specialTimesAssertionsConfig")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply with 17 in a timely manner" in {
      within(50 millis, 1 second) {
        workerActor ! "hard work"
        expectMsg(WorkResult(17))
      }
    }
    "reply with valid work at reasonable cadence" in {
      within(1 second) {
        workerActor ! "easy work"
        val results: Seq[Int] = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        } // all params of receiveWithin must be appropriate

        assert(results.sum > 5)
      }
    }

    "reply to a test probe in a timely manner" in {
      within(1 seconds) {
        val probe = TestProbe()
        probe.send(workerActor, "hard work")
        probe.expectMsg(WorkResult(17)) // timeout of 0.3s > 0.5s, we set it in application.conf
      }

    }

  }

  }

object TimedAssertionsSpec{

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "hard work" =>
        Thread.sleep(500)
        sender() ! WorkResult(17)
      case "easy work" =>
        val r = new Random()
        for (_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}