package com.softwaremill.bootzooka.email.application

import com.softwaremill.bootzooka.email.domain.EmailContentWithSubject

import scala.concurrent.Future

trait EmailService {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject): Future[Unit]

}
