package com.softwaremill.bootzooka

import java.util.Locale

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.softwaremill.bootzooka.api.Routes
import com.softwaremill.bootzooka.common.logging.AsyncErrorReportingLogAppender
import com.softwaremill.bootzooka.user.Session
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import scala.util.{Failure, Success}

class Main() extends StrictLogging {
  def start(): (Future[ServerBinding], Beans) = {
    Locale.setDefault(Locale.US) // set default locale to prevent from sending cookie expiration date in polish format

    implicit val _system = ActorSystem("main")
    implicit val _materializer = ActorMaterializer()
    import _system.dispatcher

    val modules = new Beans with Routes {
      lazy val sessionConfig = SessionConfig.fromConfig(config.rootConfig).copy(sessionEncryptData = true)

      implicit lazy val ec = _system.dispatcher
      implicit lazy val sessionManager: SessionManager[Session] = new SessionManager[Session](sessionConfig)
      implicit lazy val materializer = _materializer
      lazy val system = _system
    }

    logger.info("Server secret: " + modules.sessionConfig.serverSecret.take(3) + "...")

    // Initialize error reporting client.
    AsyncErrorReportingLogAppender(modules.config, modules.errorReporter).init()

    modules.sqlDatabase.updateSchema()

    (Http().bindAndHandle(modules.routes, modules.config.serverHost, modules.config.serverPort), modules)
  }
}

object Main extends App with StrictLogging {
  val (startFuture, modules) = new Main().start()

  val host = modules.config.serverHost
  val port = modules.config.serverPort

  val system = modules.system
  import system.dispatcher

  startFuture.onComplete {
    case Success(b) =>
      logger.info(s"Server started on $host:$port")
      sys.addShutdownHook {
        b.unbind()
        modules.system.terminate()
        logger.info("Server stopped")
      }
    case Failure(e) =>
      logger.error(s"Cannot start server on $host:$port", e)
      sys.addShutdownHook {
        modules.system.terminate()
        logger.info("Server stopped")
      }
  }
}
