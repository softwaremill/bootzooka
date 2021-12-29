package com.softwaremill.bootzooka.email

import cats.effect.IO
import com.softwaremill.bootzooka.email.sender.{DummyEmailSender, EmailSender, MailgunEmailSender, SmtpEmailSender}
import com.softwaremill.bootzooka.util.BaseModule
import com.softwaremill.macwire._
import sttp.client3.SttpBackend
import doobie.util.transactor.Transactor

trait EmailModule extends BaseModule {
  lazy val emailModel = new EmailModel
  private lazy val emailConfig = config.email
  lazy val emailService = wire[EmailService]
  // the EmailService implements the EmailScheduler functionality - hence, creating an alias for this dependency
  lazy val emailScheduler: EmailScheduler = emailService
  lazy val emailTemplates = new EmailTemplates()
  // depending on the configuration, creating the appropriate EmailSender instance
  lazy val emailSender: EmailSender = if (config.email.mailgun.enabled) {
    new MailgunEmailSender(config.email.mailgun, sttpBackend)
  } else if (config.email.smtp.enabled) {
    new SmtpEmailSender(config.email.smtp)
  } else {
    DummyEmailSender
  }

  def xa: Transactor[IO]
  def sttpBackend: SttpBackend[IO, Any]
}
