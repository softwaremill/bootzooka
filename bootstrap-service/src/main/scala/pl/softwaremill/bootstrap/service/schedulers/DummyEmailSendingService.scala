package pl.softwaremill.bootstrap.service.schedulers

class DummyEmailSendingService extends EmailSendingService {

  def run() {
    logger.info("I should be sending emails now but I am dummy :)")
  }

}
