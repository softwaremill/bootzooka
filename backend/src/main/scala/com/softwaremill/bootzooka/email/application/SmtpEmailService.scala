/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.email.application

import com.softwaremill.bootzooka.email.domain.EmailContentWithSubject
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent._
import scala.util.{Failure, Success}

class SmtpEmailService(emailConfig: EmailConfig)(implicit ec: ExecutionContext) extends EmailService with StrictLogging {
  def scheduleEmail(address: String, email: EmailContentWithSubject) = {
    val result = Future {
      val emailToSend = new SmtpEmailSender.EmailDescription(List(address), email.content, email.subject)
      SmtpEmailSender.send(
        emailConfig.emailSmtpHost, emailConfig.emailSmtpPort, emailConfig.emailSmtpUserName,
        emailConfig.emailSmtpPassword, emailConfig.emailVerifySSLCertificate, emailConfig.emailSslConnection,
        emailConfig.emailFrom, emailConfig.emailEncoding,
        emailToSend
      )
    } andThen {
      case Success(_) => logger.debug(s"Email to $address sent.")
      case Failure(cause) => logger.warn(s"Couldn't send email to $address", cause)
    }
    logger.debug(s"Email to $address scheduled.")
    result
  }
}
