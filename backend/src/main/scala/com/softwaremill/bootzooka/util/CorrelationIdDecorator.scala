package com.softwaremill.bootzooka.util

import java.{util => ju}
import ch.qos.logback.classic.util.LogbackMDCAdapter
import com.softwaremill.bootzooka.util.CorrelationIdDecorator.CorrelationIdSource
import monix.eval.Task
import monix.execution.misc.Local
import org.slf4j.{Logger, LoggerFactory, MDC}

import scala.util.Random

/**
  * Correlation id support. The `init()` method should be called when the application starts.
  * See [[https://blog.softwaremill.com/correlation-ids-in-scala-using-monix-3aa11783db81]] for details.
  */
class CorrelationIdDecorator(newCorrelationId: () => String = CorrelationIdDecorator.DefaultGenerator, mdcKey: String = "cid") {

  def init(): Unit = {
    MonixMDCAdapter.init()
  }

  def apply(): Task[Option[String]] = Task(Option(MDC.get(mdcKey)))

  def applySync(): Option[String] = Option(MDC.get(mdcKey))

  def withCorrelationId[T, R](service: T => Task[R])(implicit source: CorrelationIdSource[T]): T => Task[R] = { req: T =>
    val cid = source.extractCid(req).getOrElse(newCorrelationId())

    val setupAndService = for {
      _ <- Task(MDC.put(mdcKey, cid))
      r <- service(req)
    } yield r

    setupAndService.guarantee(Task(MDC.remove(mdcKey)))
  }
}

object CorrelationIdDecorator {
  val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  private val random = new Random()

  val DefaultGenerator: () => String = { () =>
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment-$segment"
  }

  trait CorrelationIdSource[T] {
    def extractCid(t: T): Option[String]
  }
}

/**
  * Based on [[https://olegpy.com/better-logging-monix-1/]]. Makes the current correlation id available for logback
  * loggers.
  */
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
