package pl.softwaremill.bootstrap.service.schedulers

class DummyEmailSendingService extends EmailSendingService with EmailScheduler {

  private var emailsToSend: List[EmailContent] = List()

  def run() {
    var tempList: List[EmailContent] = null
    this.synchronized {
      tempList = emailsToSend
      emailsToSend = List()
    }
    logger.info("I should be sending emails now but I am dummy :)")
    for (email <- tempList) {
      logger.info("Dummy send email: " + email.toString)
    }

  }

  def scheduleEmail(address: String, subject: String, content: String) {
    this.synchronized {
      emailsToSend = emailsToSend :+ EmailContent(address, subject, content)
    }
    logger.debug("Email to " + address + " scheduled")
  }

  case class EmailContent(address: String, subject: String, content: String)
}
