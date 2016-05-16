package com.softwaremill.bootzooka.utils

import akka.actor.{Actor, ActorRef}
import com.softwaremill.bootzooka.utils.http.PerRequest._

import scala.concurrent.Future

trait ActorPerRequest {
  actor: Actor =>

  import akka.pattern.pipe
  import context.dispatcher

  def pipeToSender(sender: ActorRef, future: Future[Event]) = {
    future recover Bad.recover pipeTo sender onComplete {
      _ => context stop self
    }
  }

}
