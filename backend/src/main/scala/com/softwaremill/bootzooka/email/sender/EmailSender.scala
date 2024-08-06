package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.{EmailConfig, EmailData}
import ox.IO
import sttp.client3.SttpBackend
import sttp.shared.Identity

trait EmailSender:
  def apply(email: EmailData)(using IO): Unit

object EmailSender:
  def create(sttpBackend: SttpBackend[Identity, Any], config: EmailConfig): EmailSender = if config.mailgun.enabled then
    new MailgunEmailSender(config.mailgun, sttpBackend)
  else if config.smtp.enabled then new SmtpEmailSender(config.smtp)
  else DummyEmailSender
