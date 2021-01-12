package com.softwaremill.bootzooka

import cats.effect.Resource
import com.softwaremill.bootzooka.config.ConfigModule
import com.softwaremill.bootzooka.infrastructure.DB
import monix.eval.Task
import sttp.capabilities.WebSockets
import sttp.capabilities.monix.MonixStreams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.monix.AsyncHttpClientMonixBackend

/** Initialised resources needed by the application to start.
  */
trait InitModule extends ConfigModule {
  lazy val db: DB = new DB(config.db)
  lazy val baseSttpBackend: Resource[Task, SttpBackend[Task, MonixStreams with WebSockets]] =
    AsyncHttpClientMonixBackend.resource()
}
