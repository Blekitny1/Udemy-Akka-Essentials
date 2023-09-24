package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App{

  // partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 34
    case 22 => 17
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 22 => 17
  }

  val function: (Int => Int) = partialFunction // a function type

  val modifiedList = List(1, 2, 3).map {
    case 1 => 42
    case _ => 17
  }

  val lifted = partialFunction.lift // total function Int => Option[Int]
  lifted(2) // Some(34)
  lifted(3) // None

  // orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(22) // 17 per partialFunction
  pfChain(60) // 9000 per orElse
  pfChain(333) // throw a MatchError

  // type aliases - wrap a complex type into friendly name
  type ReceiveFunction = PartialFunction[Any, Unit]
  def receive: ReceiveFunction = {
    case 1 => println("hello 1")
    case _ => println("what?")
  }

  // implicits
  // implicit val
  implicit val timeout = 3000
  def setTimeout(f:() => Unit)(implicit timeout: Int): Unit = f()
  setTimeout(() => println("timeout")) // its fine, extra parameter list omitted bc of implicit value

  // implicit conversions
  // implicit defs
  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)
  "Peter".greet // works, since it actually is
  // fromStringToPerson(Person("Peter").greet)) automatically done by compiler

  // implicit classes
  implicit class Dog(name: String) {
    def bark(): Unit = println("Bark!")
  }

  "Brutus".bark // works, since it actually is
  // new Dog("Brutus").bart automatically done by compiler

  // organize scopes - first we look for implicit in local scope > imported scope > companion objects of types in call
  // local scope

  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1,2,3).sorted // List(3, 2, 1)

  // imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("Hello, future")
  }

  // companion objects of the types included in call e.g.
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  List(Person("bob"), Person("alice")).sorted // List(Person(Alice),Person(Bob))


}
