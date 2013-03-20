package com.softwaremill.bootzooka.service.config

import com.softwaremill.common.conf.{ MapWrapper, Configuration, Config }
import java.util

object BootzookaConfiguration {

  val config: Config[String, String] = try {
    Configuration.get("application")
  } catch {
    case e: RuntimeException => new MapWrapper(new util.HashMap[String, String]())
  }

  val smtpHost            = config.get("smtpHost")
  val smtpPort            = config.get("smtpPort")
  val smtpUserName        = config.get("smtpUsername")
  val smtpPassword        = config.get("smtpPassword")
  val from                = config.get("from")
  val taskSQSQueue        = config.get("queue")
  val awsAccessKeyId      = config.get("AWSAccessKeyId")
  val awsSecretAccessKey  = config.get("SecretAccessKey")
  val encoding            = config.get("encoding")
  val resetLinkPattern    = config.get("resetLinkPattern")

}
