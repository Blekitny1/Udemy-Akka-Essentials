package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App{

  val system = ActorSystem("mailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))
  class LoggerActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  /**
   * Interesting case #1 - custom priority mailbox
   * P0 -> most important P1 > P2 > P3
   */

  // 1 - mailbox definition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case msg: String if msg.startsWith("[P0]") => 0
        case msg: String if msg.startsWith("[P1]") => 1
        case msg: String if msg.startsWith("[P2]") => 2
        case msg: String if msg.startsWith("[P3]") => 3
        case _ => 4
      })

  // 2 - make config know it = attaching to a dispatcher
  // 3 - attach a dispatcher to an actor

  val supportTicketLogger = system.actorOf(Props[LoggerActor].withDispatcher("support-ticket-dispatcher"))
  //supportTicketLogger ! PoisonPill // actually postponed after tickets
  // Thread.sleep(222) // actor dies in the mean time :(
  // supportTicketLogger ! "[P3] would be nice to have" // last
  // supportTicketLogger ! "[P0] we must have it now" // this is handled first
  // supportTicketLogger ! "[P1] we must have it" // second
  // after which time can I sent another msg to be prioritize?
  // Answer: we don't know and can't know :(

  /**
   * Interesting case #2 - control-aware mailbox
   * we'll use UnboundedControlAwareMailbox
   */

  // step 1 - mark important messages as control messages

  case object ManagementTicket extends ControlMessage

  // step 2 - configure who gets the mailbox
  // method #1
  val controlAwareActor = system.actorOf(Props[LoggerActor].withMailbox("control-mailbox"))
//  controlAwareActor ! "[P3] would be nice to have" // last
//  controlAwareActor ! "[P0] we must have it now" // this is handled first
//  controlAwareActor ! ManagementTicket

  // method #2 - using deployment config

  val altControlAwareActor = system.actorOf(Props[LoggerActor], "altControlAwareActor")
  altControlAwareActor ! "[P3] would be nice to have" // last
  altControlAwareActor ! "[P0] we must have it now" // this is handled first
  altControlAwareActor ! ManagementTicket



}
