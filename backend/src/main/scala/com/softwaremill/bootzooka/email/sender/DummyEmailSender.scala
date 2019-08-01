package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

import scala.collection.mutable.ListBuffer

object DummyEmailSender extends EmailSender with StrictLogging {

  private val sentEmails: ListBuffer[EmailData] = ListBuffer()

  def reset(): Unit = this.synchronized {
    sentEmails.clear()
  }

  override def apply(email: EmailData): Task[Unit] = Task {
    this.synchronized {
      sentEmails += email
    }

    logger.info(s"Would send email, if this wasn't a dummy email service implementation: $email")
  }

  def findSentEmail(recipient: String, subjectContains: String): Option[EmailData] = this.synchronized {
    sentEmails.find(email => email.recipient == recipient && email.subject.contains(subjectContains))
  }
}
