package com.softwaremill.bootzooka.service.email

import com.softwaremill.bootzooka.service.config.EmailConfig
import com.softwaremill.bootzooka.service.email.sender.{EmailDescription, EmailSender}
import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class SmtpEmailService(emailConfig: EmailConfig)(implicit ec: ExecutionContext) extends EmailService with LazyLogging {
  def scheduleEmail(address: String, email: EmailContentWithSubject) = {
    val result = Future {
      val emailToSend = new EmailDescription(List(address), email.content, email.subject)
      Try {
        EmailSender.send(
          emailConfig.emailSmtpHost, emailConfig.emailSmtpPort, emailConfig.emailSmtpUserName,
          emailConfig.emailSmtpPassword, emailConfig.emailVerifySSLCertificate, emailConfig.emailSslConnection,
          emailConfig.emailFrom, emailConfig.emailEncoding,
          emailToSend
        )
      } match {
        case Success(_) => logger.debug(s"Email to $address sent.")
        case Failure(cause) => logger.warn(s"Couldn't send email to $address because of: $cause")
      }
    }
    logger.debug(s"Email to $address scheduled.")
    result
  }
}
