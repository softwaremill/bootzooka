package com.softwaremill.bootzooka.service.email

import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.softwaremill.bootzooka.service.email.sender.{EmailSender, EmailDescription}
import com.softwaremill.bootzooka.service.config.EmailConfig

class ProductionEmailSendingService(emailConfig: EmailConfig) extends EmailScheduler with Logging {
  def scheduleEmail(address: String, email: EmailContentWithSubject) {
    Future {
      val emailToSend = new EmailDescription(List(address), email.content, email.subject)
      EmailSender.send(
        emailConfig.emailSmtpHost, emailConfig.emailSmtpPort, emailConfig.emailSmtpUserName,
        emailConfig.emailSmtpPassword, emailConfig.emailVerifySSLCertificate, emailConfig.emailSslConnection,
        emailConfig.emailFrom, emailConfig.emailEncoding,
        emailToSend)
      logger.debug(s"Email to $address sent.")
    }
    logger.debug(s"Email to $address scheduled.")
  }
}
