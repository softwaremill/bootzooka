package com.softwaremill.bootzooka

import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.ConfigModule
import com.softwaremill.bootzooka.infrastructure.DB
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend

/** Initialised resources needed by the application to start.
  */
trait InitModule extends ConfigModule {
  lazy val db: DB = new DB(config.db)
  lazy val baseSttpBackend: Resource[IO, SttpBackend[IO, Fs2Streams[IO] with WebSockets]] =
    AsyncHttpClientFs2Backend.resource()
}
