package com.softwaremill.bootzooka.email

import scala.concurrent.Future

trait EmailService {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject): Future[Unit]

}
