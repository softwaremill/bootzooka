package com.softwaremill.bootzooka.service.email

import com.softwaremill.bootzooka.service.config.BootzookaConfiguration._
import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.softwaremill.bootzooka.service.email.sender.{EmailSender, EmailDescription}

class ProductionEmailSendingService extends EmailScheduler with Logging {
  def scheduleEmail(address: String, email: EmailContentWithSubject) {
    Future {
      val emailToSend = new EmailDescription(List(address), email.content, email.subject)
      EmailSender.send(smtpHost, smtpPort, smtpUserName, smtpPassword, true, true, from, encoding, emailToSend)
      logger.debug(s"Email to $address sent.")
    }
    logger.debug(s"Email to $address scheduled.")
  }
}
