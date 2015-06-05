package com.softwaremill.bootzooka.service.config

import com.typesafe.config.Config

trait EmailConfig {
  def rootConfig: Config

  private lazy val emailConfig = rootConfig.getConfig("email")

  lazy val emailEnabled = emailConfig.getBoolean("enabled")
  lazy val emailSmtpHost = emailConfig.getString("smtp-host")
  lazy val emailSmtpPort = emailConfig.getString("smtp-port")
  lazy val emailSmtpUserName = emailConfig.getString("smtp-username")
  lazy val emailSmtpPassword = emailConfig.getString("smtp-password")
  lazy val emailFrom = emailConfig.getString("from")
  lazy val emailEncoding = emailConfig.getString("encoding")
  lazy val emailSslConnection = emailConfig.getBoolean("ssl-connection")
  lazy val emailVerifySSLCertificate = emailConfig.getBoolean("verify-ssl-certificate")
}
