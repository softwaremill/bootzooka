package pl.softwaremill.bootstrap.service.schedulers


class ProductionEmailSendingService extends EmailSendingService {

  def run() {
    logger.info("Soon I will be sending email from Amazon SQS")
  }
}
