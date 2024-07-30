package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.config.Sensitive
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.FiniteDuration

case class EmailConfig(
    mailgun: MailgunConfig,
    smtp: SmtpConfig,
    batchSize: Int,
    emailSendInterval: FiniteDuration
) derives ConfigReader

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
) derives ConfigReader

case class MailgunConfig(enabled: Boolean, apiKey: Sensitive, url: String, domain: String, senderName: String, senderDisplayName: String)
    derives ConfigReader
