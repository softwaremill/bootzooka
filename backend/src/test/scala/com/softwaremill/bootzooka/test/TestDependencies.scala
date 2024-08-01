package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.Dependencies
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.util.Clock
import org.scalatest.{Args, BeforeAndAfterAll, Status, Suite}
import ox.IO.globalForTesting.given
import ox.{Ox, supervised}
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{HttpClientSyncBackend, SttpBackend}
import sttp.shared.Identity
import sttp.tapir.server.stub.TapirStubInterpreter

trait TestDependencies extends BeforeAndAfterAll with TestEmbeddedPostgres:
  self: Suite with BaseTest =>
  var dependencies: Dependencies = _

  private val stub: SttpBackendStub[Identity, Any] = HttpClientSyncBackend.stub
  private var currentOx: Ox = _

  abstract override protected def runTests(testName: Option[String], args: Args): Status =
    supervised {
      currentOx = summon[Ox]
      super.runTests(testName, args)
    }

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    given Ox = currentOx
    dependencies = new Dependencies {
      override lazy val config: Config = TestConfig
      override lazy val sttpBackend: SttpBackend[Identity, Any] = stub
      override lazy val db: DB = currentDb
      override lazy val clock: Clock = testClock
    }
  }

  private lazy val serverStub: SttpBackend[Identity, Any] =
    TapirStubInterpreter[Identity, Any](stub)
      .whenServerEndpointsRunLogic(dependencies.httpApi.allEndpoints)
      .backend()

  lazy val requests = new Requests(serverStub)
