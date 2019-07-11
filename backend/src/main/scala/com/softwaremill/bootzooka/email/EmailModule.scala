package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.email.sender.{DummyEmailSender, EmailSender, MailgunEmailSender, SmtpEmailSender}
import com.softwaremill.bootzooka.util.BaseModule
import com.softwaremill.sttp.SttpBackend
import doobie.util.transactor.Transactor
import monix.eval.Task

trait EmailModule extends BaseModule {
  lazy val emailService = new EmailService(idGenerator, emailSender, config.email, xa)
  lazy val emailScheduler: EmailScheduler = emailService
  lazy val emailTemplates = new EmailTemplates()
  lazy val emailSender: EmailSender = if (config.email.mailgun.enabled) {
    new MailgunEmailSender(config.email.mailgun)(sttpBackend)
  } else if (config.email.smtp.enabled) {
    new SmtpEmailSender(config.email.smtp)
  } else {
    DummyEmailSender
  }

  def xa: Transactor[Task]
  def sttpBackend: SttpBackend[Task, Nothing]
}
