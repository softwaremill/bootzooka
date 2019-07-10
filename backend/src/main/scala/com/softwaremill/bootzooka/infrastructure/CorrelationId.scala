package com.softwaremill.bootzooka.infrastructure

import java.{util => ju}

import cats.data.{Kleisli, OptionT}
import ch.qos.logback.classic.util.LogbackMDCAdapter
import com.softwaremill.sttp
import com.softwaremill.sttp.{MonadError, Response, SttpBackend}
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import monix.execution.misc.Local
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Request}
import org.slf4j.MDC

import scala.util.Random

// https://blog.softwaremill.com/correlation-ids-in-scala-using-monix-3aa11783db81
object CorrelationId extends StrictLogging {
  System.setProperty("monix.environment.localContextPropagation", "1")
  def init(): Unit = {
    MonixMDCAdapter.init()
  }

  private val MdcKey = "cid"

  def apply(): Task[Option[String]] = Task(Option(MDC.get(MdcKey)))

  val CorrelationIdHeader = "X-Correlation-ID"

  def setCorrelationIdMiddleware(service: HttpRoutes[Task]): HttpRoutes[Task] = Kleisli { req: Request[Task] =>
    val cid = req.headers.get(CaseInsensitiveString(CorrelationIdHeader)) match {
      case None            => newCorrelationId()
      case Some(cidHeader) => cidHeader.value
    }

    val setupAndService = for {
      _ <- Task(MDC.put(MdcKey, cid))
      _ <- Task(logger.debug(s"Starting request with id: $cid, to: ${req.uri.path}"))
      r <- service(req).value
    } yield r

    OptionT(setupAndService.guarantee(Task(MDC.remove(MdcKey))))
  }

  private val random = new Random()

  private def newCorrelationId(): String = {
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment-$segment"
  }
}

class SetCorrelationIdBackend(delegate: SttpBackend[Task, Nothing]) extends SttpBackend[Task, Nothing] {
  override def send[T](request: sttp.Request[T, Nothing]): Task[Response[T]] = {
    // suspending the calculation of the correlation id until the request send is evaluated
    CorrelationId()
      .map {
        case Some(cid) => request.header(CorrelationId.CorrelationIdHeader, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[Task] = delegate.responseMonad
}

// from https://olegpy.com/better-logging-monix-1/
class MonixMDCAdapter extends LogbackMDCAdapter {
  private[this] val map = Local[ju.Map[String, String]](ju.Collections.emptyMap())

  override def put(key: String, `val`: String): Unit = {
    if (map() eq ju.Collections.EMPTY_MAP) {
      map := new ju.HashMap()
    }
    map().put(key, `val`)
    ()
  }

  override def get(key: String): String = map().get(key)
  override def remove(key: String): Unit = {
    map().remove(key)
    ()
  }

  // Note: we're resetting the Local to default, not clearing the actual hashmap
  override def clear(): Unit = map.clear()
  override def getCopyOfContextMap: ju.Map[String, String] = new ju.HashMap(map())
  override def setContextMap(contextMap: ju.Map[String, String]): Unit =
    map := new ju.HashMap(contextMap)

  override def getPropertyMap: ju.Map[String, String] = map()
  override def getKeys: ju.Set[String] = map().keySet()
}

object MonixMDCAdapter {
  def init(): Unit = {
    val field = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    field.set(null, new MonixMDCAdapter)
  }
}
