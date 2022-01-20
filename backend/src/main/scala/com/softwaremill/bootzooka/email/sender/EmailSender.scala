package com.softwaremill.bootzooka.email.sender

import cats.effect.IO
import com.softwaremill.bootzooka.email.{EmailConfig, EmailData}
import sttp.client3.SttpBackend

trait EmailSender {
  def apply(email: EmailData): IO[Unit]
}

object EmailSender {
  def create(sttpBackend: SttpBackend[IO, Any], config: EmailConfig): EmailSender = if (config.mailgun.enabled) {
    new MailgunEmailSender(config.mailgun, sttpBackend)
  } else if (config.smtp.enabled) {
    new SmtpEmailSender(config.smtp)
  } else {
    DummyEmailSender
  }
}
