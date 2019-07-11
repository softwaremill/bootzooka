package com.softwaremill.bootzooka.config

import com.softwaremill.bootzooka.util._
import com.softwaremill.bootzooka.version.BuildInfo
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigReader
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

trait ConfigModule extends StrictLogging {

  private implicit def idReader[T]: ConfigReader[Id @@ T] = ConfigReader[String].map(_.asId)

  lazy val config: Config = pureconfig.loadConfigOrThrow[Config]

  def logConfig(): Unit = {
    val baseInfo = s"""
                   |Bootzooka configuration:
                   |-----------------------
                   |DB:             ${config.db}
                   |API:            ${config.api}
                   |Email:          ${config.email}
                   |Password reset: ${config.passwordReset}
                   |User:           ${config.user}
                   |
                   |Build & env info:
                   |-----------------
                   |""".stripMargin

    val info = TreeMap(BuildInfo.toMap.toSeq: _*).foldLeft(baseInfo) {
      case (str, (k, v)) => str + s"$k: $v\n"
    }

    logger.info(info)
  }
}
