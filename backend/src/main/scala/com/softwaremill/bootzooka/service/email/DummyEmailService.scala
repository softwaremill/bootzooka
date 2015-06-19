package com.softwaremill.bootzooka.service.email

import com.softwaremill.bootzooka.service.email.sender.EmailDescription
import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class DummyEmailService extends EmailService with LazyLogging {

  private val sentEmails: ListBuffer[EmailDescription] = ListBuffer()

  def emailToString(email: EmailDescription): String = {
    email.emails.mkString + ", " + email.subject + ", " + email.message
  }

  def reset() {
    sentEmails.clear()
  }

  override def scheduleEmail(address: String, emailData: EmailContentWithSubject) = {
    val email = new EmailDescription(List(address), emailData.content, emailData.subject)

    this.synchronized {
      sentEmails += email
    }

    logger.debug(s"Would send email to $address, if this wasn't a dummy email service implementation.")
    Future.successful(())
  }

  def wasEmailSent(address: String, subject: String): Boolean = {
    sentEmails.exists(email => email.emails.contains(address) && email.subject == subject)
  }
}

