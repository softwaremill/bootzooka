package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.Dependencies
import org.scalatest.{Args, BeforeAndAfterAll, Status, Suite}
import ox.IO.globalForTesting.given
import ox.{Ox, supervised}
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{HttpClientSyncBackend, SttpBackend}
import sttp.shared.Identity
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.compiletime.uninitialized

trait TestDependencies extends BeforeAndAfterAll with TestEmbeddedPostgres:
  self: Suite & BaseTest =>
  var dependencies: Dependencies = uninitialized

  private val stub: SttpBackendStub[Identity, Any] = HttpClientSyncBackend.stub
  private var currentOx: Ox = uninitialized

  abstract override protected def runTests(testName: Option[String], args: Args): Status =
    supervised {
      currentOx = summon[Ox]
      super.runTests(testName, args)
    }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    dependencies = Dependencies.create(TestConfig, _ => stub, currentDb, testClock)
  }

  private lazy val serverStub: SttpBackend[Identity, Any] =
    TapirStubInterpreter[Identity, Any](stub)
      .whenServerEndpointsRunLogic(dependencies.httpApi.allEndpoints)
      .backend()

  lazy val requests = new Requests(serverStub)
