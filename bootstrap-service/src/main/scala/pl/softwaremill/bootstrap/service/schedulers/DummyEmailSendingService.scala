package pl.softwaremill.bootstrap.service.schedulers

import pl.softwaremill.bootstrap.service.templates.EmailContentWithSubject
import collection.mutable.ListBuffer
import pl.softwaremill.common.sqs.util.EmailDescription

class DummyEmailSendingService extends EmailSendingService with EmailScheduler {

  private val emailsToSend: ListBuffer[EmailDescription] = ListBuffer()

  private val sentEmails: ListBuffer[EmailDescription] = ListBuffer()

  def run() {
    var tempList: ListBuffer[EmailDescription] = null
    this.synchronized {
      tempList ++= emailsToSend
      sentEmails ++= emailsToSend
      emailsToSend.clear()
    }
    logger.info("I should be sending emails now but I am dummy :)")
    for (email <- tempList) {
      logger.info("Dummy send email: " + email.toString)
    }

  }

  def scheduleEmail(address: String, emailData: EmailContentWithSubject) {
    this.synchronized {
      emailsToSend += new EmailDescription(address, emailData.subject, emailData.content)
    }
    logger.debug("Email to " + address + " scheduled")
  }

  def wasEmailSent(address:String, subject:String):Boolean = {
    sentEmails.exists(email => email.getEmails.contains(address) && email.getSubject == subject)
  }

  def wasEmailSent(email:EmailDescription):Boolean = {
    sentEmails.contains(email)
  }
}

