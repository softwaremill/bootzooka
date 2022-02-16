package com.softwaremill.bootzooka.config

import com.softwaremill.bootzooka.email.EmailConfig
import com.softwaremill.bootzooka.http.HttpConfig
import com.softwaremill.bootzooka.infrastructure.DBConfig
import com.softwaremill.bootzooka.passwordreset.PasswordResetConfig
import com.softwaremill.bootzooka.user.UserConfig
import com.softwaremill.bootzooka.version.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

/** Maps to the `application.conf` file. Configuration for all modules of the application. */
case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, user: UserConfig)

object Config extends StrictLogging {
  def log(config: Config): Unit = {
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

    val info = TreeMap(BuildInfo.toMap.toSeq: _*).foldLeft(baseInfo) { case (str, (k, v)) =>
      str + s"$k: $v\n"
    }

    logger.info(info)
  }

  def read: Config = ConfigSource.default.loadOrThrow[Config]
}
