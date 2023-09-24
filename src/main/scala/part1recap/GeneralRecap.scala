package part1recap

import scala.annotation.tailrec
import scala.util.Try

object GeneralRecap extends App{

  // values
  val aCondition: Boolean = false

  // expressions
  val aConditionedVal = if (aCondition) 42 else 32

  // code blocks
  val aCodeBlock = {
    if (aCondition) 43
    56
  }

  // variable of Unit type? No problem.
  val theUnit: Unit = println("Hello, Scala")

  // tail recursion
  @tailrec
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, acc * n)

  // OOP
  class Animal
  class Dog extends Animal
  val adog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Croco!")
  }

  // anonymous classes
  val aCroc = new Carnivore {
    override def eat(a: Animal): Unit = println("I am a croc!")
  }

  // method notations
  val aCroc2 = new Crocodile
  aCroc2.eat(adog)
  aCroc2 eat adog

  // generics
  abstract class MyList[+A]
  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // exceptions
  val aPotentialFailure = try {
    throw new RuntimeException("One!")
  } catch {
    case e: Exception => "Two!"
  } finally {
    // side effects
    println("Three!")
  }

  // FP
  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(17) // 18
  // incrementer.apply(17)

  val anonIncrementer = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]

  // FP is all about working with functions as first class objects
  List(1 ,2, 3).map(incrementer)
  // map = HOF

  // for comprehensions
  val pairs = for {
    num<- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "0" + char

  List(1, 2, 3, 4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "0" + char))

  // Collections: Seq, Array, List, Vector, Tuples, Sets

  // "Collections"
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(name, _) => s"Hi, ny name is $name"
    case _ => "Who are you?"
  }


}
