package com.softwaremill.bootzooka.email

import scala.concurrent.duration.FiniteDuration

case class EmailConfig(
    enabled: Boolean,
    smtp: SmtpConfig,
    from: String,
    encoding: String,
    batchSize: Int,
    emailSendInterval: FiniteDuration
)

case class SmtpConfig(host: String, port: Int, username: String, password: String, sslConnection: Boolean, verifySslCertificate: Boolean) {
  override def toString: String = s"SmtpConfig($host,$port,$username,***,$sslConnection,$verifySslCertificate)"
}
