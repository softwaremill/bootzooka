package com.softwaremill.bootzooka.test

import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

trait FlatSpecWithDb extends FlatSpec with SpecWithDb with SpecWithActorSystem with Matchers with ScalaFutures
  with IntegrationPatience

