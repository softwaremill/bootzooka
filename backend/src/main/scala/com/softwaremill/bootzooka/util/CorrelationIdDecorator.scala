package com.softwaremill.bootzooka.util


import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOLocal}
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

  private[this] val localMap = IOLocal(ju.Collections.emptyMap[String, String]).unsafeRunSync()

  override def put(key: String, `val`: String): Unit =
    localMap.update(map => {
      if (map eq ju.Collections.EMPTY_MAP) {
        val newMap = new ju.HashMap[String, String]()
        newMap.put(key, `val`)
        newMap
      } else {
        map.put(key, `val`)
        map
      }
    }).unsafeRunSync()

  override def get(key: String): String = localMap.get.unsafeRunSync().get(key)


  override def remove(key: String): Unit =
    localMap.update(map => {
      map.remove(key)
      map
    }).unsafeRunSync()

  // Note: we're resetting the Local to default, not clearing the actual hashmap
  override def clear(): Unit = localMap.reset.unsafeRunSync()

  override def getCopyOfContextMap: ju.Map[String, String] = new ju.HashMap(localMap.get.unsafeRunSync())

  override def setContextMap(contextMap: ju.Map[String, String]): Unit = localMap.set(new ju.HashMap(contextMap)).unsafeRunSync()

  override def getPropertyMap: ju.Map[String, String] = localMap.get.unsafeRunSync()

  override def getKeys: ju.Set[String] = localMap.get.unsafeRunSync().keySet()
}

object MDCAdapter {
  def init(): Unit = {
    val field = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    field.set(null, new MDCAdapter)
  }
}
