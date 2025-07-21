package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.Dependencies
import io.opentelemetry.api.OpenTelemetry
import org.scalatest.{BeforeAndAfterAll, Suite}
import sttp.client4.SyncBackend
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.tapir.server.stub4.TapirSyncStubInterpreter

import scala.compiletime.uninitialized

trait TestDependencies extends BeforeAndAfterAll with TestEmbeddedPostgres:
  self: Suite & BaseTest =>

  var dependencies: Dependencies = uninitialized

  override protected def beforeAll(): Unit =
    super.beforeAll()
    dependencies = Dependencies.create(TestConfig, OpenTelemetry.noop(), HttpClientSyncBackend.stub, currentDb, testClock)

  private lazy val serverStub: SyncBackend =
    TapirSyncStubInterpreter()
      .whenServerEndpointsRunLogic(dependencies.httpApi.allEndpoints)
      .backend()

  lazy val requests = new Requests(serverStub)
end TestDependencies
