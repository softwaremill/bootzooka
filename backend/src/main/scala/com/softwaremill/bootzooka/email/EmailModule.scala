package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.BaseModule
import com.softwaremill.bootzooka.email.sender.{DummyEmailSender, EmailSender, SmtpEmailSender}
import doobie.util.transactor.Transactor
import monix.eval.Task

trait EmailModule extends BaseModule {
  lazy val emailService = new EmailService(idGenerator, emailSender, config.email, xa)
  lazy val emailScheduler: EmailScheduler = emailService
  lazy val emailTemplates = new EmailTemplates()
  lazy val emailSender: EmailSender = if (config.email.enabled) {
    new SmtpEmailSender(config.email)
  } else {
    DummyEmailSender
  }

  def xa: Transactor[Task]
}
