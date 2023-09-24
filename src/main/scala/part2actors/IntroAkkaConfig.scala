package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  /**
   * 1 - inline configuration, try with INFO and ERROR instead of DEBUG
   */

  val configString =
    """
      | akka {
      |   loglevel = "DEBUG"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("configDemo", ConfigFactory.load(config)) // config object loaded
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "A message to remember."

  /**
   * 2 - configuration file (resources/application.conf is a default one
   */

  val system2 = ActorSystem("defaultFile")
  val defaultConfingActor = system2.actorOf(Props[SimpleLoggingActor])
  defaultConfingActor ! "remember me?"

  /**
   * 3 - separate configuration in a default file
   */

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig") // we get this from application.conf
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "I have special config!"

  /**
   * 4 - separate configuration in another file
   */

  val secretConfig = ConfigFactory.load("secretFolder/secretConfig.conf")
  println(s"separate config log level: ${secretConfig.getString("akka.loglevel")}")

  /**
   * 5 - different file formats
   * JSON, Properties files
   */

  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfig.properties")
  println(s"props config: ${propsConfig.getString("my.simpleProperty")}")
  println(s"props config: ${propsConfig.getString("akka.loglevel")}")




}
