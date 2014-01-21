package com.softwaremill.bootzooka.service.schedulers

import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import collection.mutable.ListBuffer
import com.softwaremill.common.sqs.util.EmailDescription

class DummyEmailSendingService extends EmailSendingService with EmailScheduler {

  private val emailsToSend: ListBuffer[EmailDescription] = ListBuffer()

  private val sentEmails: ListBuffer[EmailDescription] = ListBuffer()

  def run() {
    var tempList: ListBuffer[EmailDescription] = ListBuffer()
    this.synchronized {
      tempList ++= emailsToSend
      sentEmails ++= emailsToSend
      emailsToSend.clear()
    }
    for (email <- tempList) {
      logger.info("Dummy send email: " + emailToString(email))
    }

  }

  def emailToString(email: EmailDescription): String = {
    email.getEmails.mkString + ", " + email.getSubject + ", " + email.getMessage
  }

  def scheduleEmail(address: String, emailData: EmailContentWithSubject) {
    this.synchronized {
      emailsToSend += new EmailDescription(address, emailData.content, emailData.subject)
    }
    logger.debug("Email to " + address + " scheduled")
  }

  def wasEmailSent(address: String, subject: String): Boolean = {
    sentEmails.exists(email => email.getEmails.contains(address) && email.getSubject == subject)
  }
}

