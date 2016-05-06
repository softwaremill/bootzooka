package com.softwaremill.bootzooka.utils

import akka.actor.Props

trait ActorPerRequestFactory {
  def props: Props
}
