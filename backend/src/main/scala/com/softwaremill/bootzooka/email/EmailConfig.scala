package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.config.Sensitive

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
    password: Sensitive,
    sslConnection: Boolean,
    verifySslCertificate: Boolean,
    from: String,
    encoding: String
)

case class MailgunConfig(enabled: Boolean, apiKey: Sensitive, url: String, domain: String, senderName: String, senderDisplayName: String)
