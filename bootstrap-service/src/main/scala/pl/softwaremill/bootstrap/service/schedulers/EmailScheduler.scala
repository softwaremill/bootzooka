package pl.softwaremill.bootstrap.service.schedulers

trait EmailScheduler {

  def scheduleEmail(address: String, subject: String, content: String)

}
