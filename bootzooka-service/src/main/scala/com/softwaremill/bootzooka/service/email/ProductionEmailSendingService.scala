package com.softwaremill.bootzooka.service.email

import com.softwaremill.common.sqs.email.EmailSender
import com.softwaremill.bootzooka.service.config.BootzookaConfiguration._
import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import com.softwaremill.common.sqs.util.EmailDescription
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProductionEmailSendingService extends EmailScheduler with Logging {
  def scheduleEmail(address: String, email: EmailContentWithSubject) {
    Future {
      val emailToSend = new EmailDescription(address, email.content, email.subject)
      EmailSender.send(smtpHost, smtpPort, smtpUserName, smtpPassword, from, encoding, emailToSend)
      logger.debug(s"Email to $address sent.")
    }
    logger.debug(s"Email to $address scheduled.")
  }
}
