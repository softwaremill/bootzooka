package pl.softwaremill.bootstrap.service.schedulers

import pl.softwaremill.bootstrap.service.templates.EmailContentWithSubject

class DummyEmailSendingService extends EmailSendingService with EmailScheduler {

  private var emailsToSend: List[EmailToSend] = List()

  def run() {
    var tempList: List[EmailToSend] = null
    this.synchronized {
      tempList = emailsToSend
      emailsToSend = List()
    }
    logger.info("I should be sending emails now but I am dummy :)")
    for (email <- tempList) {
      logger.info("Dummy send email: " + email.toString)
    }

  }

  def scheduleEmail(address: String, emailData: EmailContentWithSubject) {
    this.synchronized {
      emailsToSend = emailsToSend :+ EmailToSend(address, emailData.subject, emailData.content)
    }
    logger.debug("Email to " + address + " scheduled")
  }

  case class EmailToSend(address: String, subject: String, content: String)
}
