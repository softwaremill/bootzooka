package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import com.softwaremill.bootzooka.logging.Logging
import ox.IO

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}
import scala.jdk.CollectionConverters.*

object DummyEmailSender extends EmailSender with Logging:
  private val sentEmails: BlockingQueue[EmailData] = new LinkedBlockingQueue[EmailData]()

  def reset(): Unit = sentEmails.clear()

  override def apply(email: EmailData)(using IO): Unit =
    sentEmails.put(email)
    logger.info(s"Would send email, if this wasn't a dummy email service implementation: $email")

  def findSentEmail(recipient: String, subjectContains: String): Option[EmailData] =
    sentEmails.iterator().asScala.find(email => email.recipient == recipient && email.subject.contains(subjectContains))
