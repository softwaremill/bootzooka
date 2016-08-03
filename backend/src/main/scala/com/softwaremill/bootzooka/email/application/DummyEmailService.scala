/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.email.application

import com.softwaremill.bootzooka.email.application.SmtpEmailSender.EmailDescription
import com.softwaremill.bootzooka.email.domain.EmailContentWithSubject
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class DummyEmailService extends EmailService with StrictLogging {

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

    logger.debug(s"Would send email to $address, if this wasn't a dummy email service implementation: ${emailData.content}")
    Future.successful(())
  }

  def wasEmailSent(address: String, subject: String): Boolean = this.synchronized {
    sentEmails.exists(email => email.emails.contains(address) && email.subject == subject)
  }

  def wasEmailSentTo(address: String): Boolean = this.synchronized {
    sentEmails.exists(email => email.emails.contains(address))
  }
}

