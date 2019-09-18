package com.softwaremill.bootzooka.config

import com.softwaremill.bootzooka.version.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

/**
  * Reads and gives access to the configuration object.
  */
trait ConfigModule extends StrictLogging {

  lazy val config: Config = ConfigSource.default.loadOrThrow[Config]

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
