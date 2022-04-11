package com.softwaremill.bootzooka.test

import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.Dependencies
import org.scalatest.{BeforeAndAfterAll, Suite}
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.tapir.server.stub.TapirStubInterpreter

trait TestDependencies extends BeforeAndAfterAll with TestEmbeddedPostgres {
  self: Suite with BaseTest =>
  var dependencies: Dependencies = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    dependencies = {
      import cats.effect.unsafe.implicits.global

      Dependencies
        .wire(
          config = TestConfig,
          sttpBackend = Resource.pure(AsyncHttpClientFs2Backend.stub[IO]),
          xa = Resource.pure(currentDb.xa),
          clock = testClock
        )
        .allocated
        .unsafeRunSync()
        ._1
    }
  }

  private lazy val backendStub: SttpBackend[IO, Any] =
    TapirStubInterpreter[IO, Any](AsyncHttpClientFs2Backend.stub[IO])
      .whenServerEndpointsRunLogic(dependencies.api.allEndpoints)
      .backend()

  lazy val testClient = new TestClient(backendStub)
}
