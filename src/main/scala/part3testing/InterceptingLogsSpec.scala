import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, color, fullstacks}
import part3testing.TimedAssertionsSpec.{WorkResult, WorkerActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class InterceptingLogsSpec extends TestKit(ActorSystem("InterceptingLogSpec", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogsSpec._

  val item = "Pizza"
  val creditCard = "1234-1234-1234-1234"
  val invalidCreditCard = "0000-0000-0000-0000"
  "A checkout flow" should {
    "correctly log a dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.", occurrences = 1) intercept {
        // our test code
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }
    "say NOOO when get payment denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        // our test code
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, invalidCreditCard)

      }
    }
  }

}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case class DispatchOrder(item: String)
  case object OrderConfirmed

  class CheckoutActor extends Actor {

    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])
    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>

        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>

        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))

      case PaymentDenied => throw new RuntimeException("NOOOOO")
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed =>
        context.become(awaitingCheckout)

    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        // Thread.sleep(4000) // EventFilter waits regular 3s.
        if (card.startsWith("0")) sender() ! PaymentDenied else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging{

    var orderId = 0
    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1

        log.info(s"Order $orderId for item $item has been dispatched.")
        sender() ! OrderConfirmed
    }
  }
}

