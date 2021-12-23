package com.softwaremill.bootzooka.util

import cats.effect.IO
import ch.qos.logback.classic.util.LogbackMDCAdapter
import com.softwaremill.bootzooka.util.CorrelationIdDecorator.CorrelationIdSource
import org.slf4j.{Logger, LoggerFactory, MDC}

import java.{util => ju}
import scala.util.Random

class CorrelationIdDecorator(newCorrelationId: () => String = CorrelationIdDecorator.DefaultGenerator, mdcKey: String = "cid") {

  def init(): Unit = {
    MDCAdapter.init()
  }

  def apply(): IO[Option[String]] = IO(Option(MDC.get(mdcKey)))

  def applySync(): Option[String] = Option(MDC.get(mdcKey))

  def withCorrelationId[T, R](service: T => IO[R])(implicit source: CorrelationIdSource[T]): T => IO[R] = { req: T =>
    val cid = source.extractCid(req).getOrElse(newCorrelationId())

    val setupAndService = for {
      _ <- IO(MDC.put(mdcKey, cid))
      r <- service(req)
    } yield r

    setupAndService.guarantee(IO(MDC.remove(mdcKey)))
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

class MDCAdapter extends LogbackMDCAdapter {

  private[this] var map: ju.Map[String, String] = ju.Collections.emptyMap()

  override def put(key: String, `val`: String): Unit = {
    if (map eq ju.Collections.EMPTY_MAP) {
      val map1: ju.HashMap[String, String] = new ju.HashMap()
      map = map1
    }
    map.put(key, `val`)
    ()
  }

  override def get(key: String): String = map.get(key)

  override def remove(key: String): Unit = {
    map.remove(key)
    ()
  }
  // Note: we're resetting the Local to default, not clearing the actual hashmap
  override def clear(): Unit = map.clear()

  override def getCopyOfContextMap: ju.Map[String, String] = new ju.HashMap(map)

  override def setContextMap(contextMap: ju.Map[String, String]): Unit =
    map = new ju.HashMap(contextMap)

  override def getPropertyMap: ju.Map[String, String] = map

  override def getKeys: ju.Set[String] = map.keySet()
}

  object MDCAdapter {
    def init(): Unit = {
      val field = classOf[MDC].getDeclaredField("mdcAdapter")
      field.setAccessible(true)
      field.set(null, new MDCAdapter)
    }
  }
