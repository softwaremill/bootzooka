package com.softwaremill.bootzooka.email

import scala.concurrent.duration.FiniteDuration

case class EmailConfig(
    mailgun: MailgunConfig,
    smtp: SmtpConfig,
    batchSize: Int,
    emailSendInterval: FiniteDuration
)

case class SmtpConfig(
    enabled: Boolean,
    host: String,
    port: Int,
    username: String,
    password: String,
    sslConnection: Boolean,
    verifySslCertificate: Boolean,
    from: String,
    encoding: String
) {
  override def toString: String = s"SmtpConfig($enabled,$host,$port,$username,***,$sslConnection,$verifySslCertificate,$from,$encoding)"
}

case class MailgunConfig(enabled: Boolean, apiKey: String, url: String, domain: String, senderName: String, senderDisplayName: String) {
  override def toString: String = s"MailgunConfig($enabled,***,$url,$domain,$senderName,$senderDisplayName)"
}
