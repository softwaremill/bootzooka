package com.softwaremill.bootzooka.util

import cats.effect.IO
import com.softwaremill.bootzooka.util.CorrelationIdDecorator.CorrelationIdSource
import org.slf4j.{Logger, LoggerFactory, MDC}

import scala.util.Random

class CorrelationIdDecorator(newCorrelationId: () => String = CorrelationIdDecorator.DefaultGenerator, mdcKey: String = "cid") {

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
