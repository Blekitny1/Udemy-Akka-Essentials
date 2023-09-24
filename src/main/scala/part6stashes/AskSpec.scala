package part6stashes

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import akka.pattern.ask
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.pipe


class AskSpec extends TestKit(ActorSystem("AskSpec"))
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
  import AskSpec._

  "An authenticator" should {
    authenticatorTestSuite(Props[AuthManager])
  }

  "A piped authenticator" should {
    authenticatorTestSuite(Props[PipedAuthManager])
  }

  def authenticatorTestSuite(props: Props): Unit = {
    "An authenticator" should {

      import AuthManager._

      "fail to authenticate a non-registered user" in {
        val authManager = system.actorOf(props)
        authManager ! Authenticate("me", "123456")
        expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
      }

      "fail to authenticate if invalid password" in {
        val authManager = system.actorOf(props)
        authManager ! RegisterUser("admin", "admin1")
        authManager ! Authenticate("admin", "admin")
        expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
      }

      "successfully authenticate a registered user" in {
        val authManager = system.actorOf(props)
        authManager ! RegisterUser("admin", "admin1")
        authManager ! Authenticate("admin", "admin1")
        expectMsg(AuthSuccess)
      }

    }
  }

}

object AskSpec {

  case class Read(key: String)
  case class Write(ket: String, value: String)

  class KVActor extends Actor with ActorLogging {


    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Writting the value $value for the key $key")
        context.become(online(kv + (key -> value)))
    }
  }

  // user authenticator actor
  case class RegisterUser(username: String, password: String)
  case class Authenticate(username: String, password: String)
  case class AuthFailure(message: String)
  case object AuthSuccess

  object AuthManager {
    val AUTH_FAILURE_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
    val AUTH_FAILURE_SYSTEM = "system error"
  }

  class AuthManager extends Actor with ActorLogging {
    import AuthManager._
    // 2 - logistics
    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(u: String, p: String) => authDb ! Write(u, p)
      case Authenticate(u, p) =>
        handleAuthentication(u, p)
    }

    def handleAuthentication(u: String, p: String): Unit = {
      val originalSender = sender()
      // 3 - ask the sender
      val future = authDb ? Read(u)
      // 4 - handle the sender with OnComplete
      future.onComplete {
        // 5 - most important
        // NEVER CALL METHODDS ON THE ACTOR INSTANCE OR ACCESS MUTABLE STATE IN ONCOMPLETE
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbP)) =>
          if (dbP == p) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }

  class PipedAuthManager extends AuthManager {
    import AuthManager._
    override def handleAuthentication(u: String, p: String): Unit = {
      // 3 - ask the actor
      val future = authDb ? Read(u)
      // 4 - process the future until you get the responses will send back
      val passwordFuture = future.mapTo[Option[String]]
      val responseFuture = passwordFuture.map {
        case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Some(dbP) =>
          if (dbP == p) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
      } // Future[Any]

      // 5 - pipe the resulting future to the actor you want the result to be sent to
      // when response future completes send the response to the actor ref in arg list
      responseFuture.pipeTo(sender())
    }
  }
}
