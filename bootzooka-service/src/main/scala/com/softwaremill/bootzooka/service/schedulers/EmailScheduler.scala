package com.softwaremill.bootzooka.service.schedulers

import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject

trait EmailScheduler {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject)

}
