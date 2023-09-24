package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router, Broadcast}
import com.typesafe.config.ConfigFactory

object Routers extends App{
  /*
  1 - manual routing
  */
  class Master extends Actor {
    // 1 - create routees
    private val slaves = for(i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"Slave$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }
    // 2 -create a router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    /**
     * other logics:
     * round robin, random, smallest mailbox, broadcast
     * scatter-gather-first, consistent hashing
     */

    override def receive: Receive = {
    // 4 - handle the termination / lifecycle of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
    // 3 -route the messages
      case msg =>
        router.route(msg, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("RouterDemo", ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master])
//  for (i <- 1 to 10) {
//    master ! s"[$i]hello master" // round robin logic indeed worked
//  }

  /**
   * 2 - router actor with its own children
   * POOL router
   * 2.1 - programmatically
   */

  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
//  for (i <- 1 to 10) {
//    poolMaster ! s"[$i]hello master" // round robin logic indeed worked
//  }

  /**
   * 2.2 from configuration, supply actor name, pool and nr of instances
   */

  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
//  for (i <- 1 to 10) {
//    poolMaster2 ! s"[$i]hello master" // round robin logic indeed worked same as above
//  }

  /**
   * 3 - router with actors created elsewhere (group router)
   */

  // somewhere else in application we create some actors
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_$i")).toList
  // we need paths of the slaves
  val slavePaths = slaveList.map(slaveRef => slaveRef.path.toString)

  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())

//  for (i <- 1 to 10) {
//        groupMaster ! s"[$i]hello master" // round robin logic indeed worked same as above
//      }

  /**
   * 3.2 - from configuration
   */

  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
  for (i <- 1 to 10) {
    groupMaster2 ! s"[$i]hello master" // round robin logic indeed worked same as above
  }

  /**
   * Special messages
   */

  groupMaster2 ! Broadcast("hello, everyone") // sent to each routed actor regardless

  // PoisonPill and Kill are not routed
  // AddRoutee, Remove, Get are handled only by routing actor

}
