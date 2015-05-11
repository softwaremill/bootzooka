package com.softwaremill.bootzooka.service.email

import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import collection.mutable.ListBuffer
import com.typesafe.scalalogging.LazyLogging
import com.softwaremill.bootzooka.service.email.sender.EmailDescription

import scala.concurrent.{ExecutionContext, Future}

class DummyEmailSendingService(implicit val ec: ExecutionContext) extends EmailScheduler with LazyLogging {

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
    email.emails.mkString + ", " + email.subject + ", " + email.message
  }

  override def scheduleEmail(address: String, emailData: EmailContentWithSubject) = {
    this.synchronized {
      emailsToSend += new EmailDescription(List(address), emailData.content, emailData.subject)
    }
    Future {
      logger.debug(s"Email to $address scheduled.")
    }
  }

  def wasEmailSent(address: String, subject: String): Boolean = {
    sentEmails.exists(email => email.emails.contains(address) && email.subject == subject)
  }
}

