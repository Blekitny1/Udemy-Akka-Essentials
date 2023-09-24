package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object actorsIntro extends App{

  // part 1 - actor system
  val actorSystem = ActorSystem("firstActorSystem") // name must not have weird chars
  println(actorSystem.name)

  // part 2 - create actors

  /**
   * Actors are:
   * - uniquely identified
   * - messages are asynchronous
   * - each actor may respond differently
   * - truly encapsulated!
   */

   // word count actor

  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0

    // behavior
    def receive: Receive = { // type PartialFunction[Any, Unit] = type Receive
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }

  }

  // part 3 - instantiate our actor
  // val wordCounter = new WordCountActor not like this, rather..
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter") // name as for actorSystem
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter") // name as for actorSystem

  // part 4 - communicate with our actor
  wordCounter ! "I am learning Akka and its cool!" // ! == .tell
  anotherWordCounter ! "Another message."
  // asynchronous! the second message came first!

  object Person {
    def props(name: String) = Props(new Person(name)) // its good to write a factory method props
  }

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  // val person = actorSystem.actorOf(Props(new Person("Bob"))) It is legal! Must be within props. Discouraged.

  val person = actorSystem.actorOf(Person.props("Bob"))  // And use props here
  person ! "hi"

}
