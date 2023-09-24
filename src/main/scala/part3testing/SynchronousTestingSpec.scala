package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part3testing.SynchronousTestingSpec.Counter

import scala.concurrent.duration.Duration

class SynchronousTestingSpec extends WordSpecLike with BeforeAndAfterAll{

  implicit val system = ActorSystem("SynchronousTestingSpec")

  override def afterAll(): Unit = system.terminate()

  import SynchronousTestingSpec._

  "A counter" should {
    "synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter]) // ActorSystem must be implicitly set
      counter ! Inc // counter has already received a message

      assert(counter.underlyingActor.count == 1) // TestActorRef can do this!
    }

    "synchronously increase its counter at the call of receive function" in {

      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc) // counter has already received a message

      assert(counter.underlyingActor.count == 1)
    }

    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe() // the calling thread dispatcher is synchronous
      probe.send(counter, Read) // thus this test with timeout 0 works
      probe.expectMsg(Duration.Zero, 0) // probe already got message 0
    }
  }

}

object SynchronousTestingSpec {

  case object Inc
  case object Read

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count

    }
  }
}
