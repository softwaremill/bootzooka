package pl.softwaremill.bootstrap.service.schedulers

import pl.softwaremill.bootstrap.service.templates.EmailContentWithSubject

trait EmailScheduler {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject)

}
