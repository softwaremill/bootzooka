package com.softwaremill.bootzooka.utils.http

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, ReceiveTimeout}
import com.softwaremill.bootzooka.utils.http.PerRequest._

import scala.concurrent.Promise
import scala.concurrent.duration._

class PerRequest(promise: Promise[Event], target: ActorRef, command: Command) extends Actor {
  import PerRequest._

  context setReceiveTimeout 2.seconds
  target ! command

  def receive = {
    case ReceiveTimeout =>
      promise success RequestTimeout
      context stop self
    case msg: Event =>
      promise success msg
      context stop self
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e =>
        promise success Bad(e.getMessage)
        Stop
    }
}

object PerRequest {

  trait Event {
    def msg: Any
  }

  trait OK extends Event
  class JustOK extends Event {
    val msg = ()
  }

  class Bad(val msg: String) extends Event
  object Bad {
    def apply(err: String) = new Bad(err)
    def recover: PartialFunction[Throwable, Event] = {
      case err: Throwable => Bad(err.getMessage)
    }
  }

  class NotFound(error: String) extends Bad(error) {
    def this() = this("Object not found")
  }
  class InvalidData(error: String) extends Bad(error)
  class Forbidden(error: String) extends Bad(error)
  class Conflict(error: String) extends Bad(error)

  case object RequestTimeout extends Bad("Request timeout")

  trait Command

}
