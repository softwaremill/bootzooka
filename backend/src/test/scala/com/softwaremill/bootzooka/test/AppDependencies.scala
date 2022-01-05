package com.softwaremill.bootzooka.test

import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.DependenciesFactory
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.http.HttpApi
import org.scalatest.{BeforeAndAfterAll, Suite}
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend

trait AppDependencies extends BeforeAndAfterAll with TestEmbeddedPostgres { self: Suite with BaseTest =>

  var httpApi: HttpApi = _
  var emailService: EmailService = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    val deps = {
      import cats.effect.unsafe.implicits.global

      DependenciesFactory.resource(
        config = TestConfig,
        sttpBackend = Resource.pure(AsyncHttpClientFs2Backend.stub[IO]),
        xa = Resource.pure(currentDb.xa),
        clock = testClock
      ).allocated.unsafeRunSync()._1
    }

    httpApi = deps._1
    emailService = deps._2
  }
}
