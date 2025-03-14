package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.{EmailConfig, EmailData}
import sttp.client4.SyncBackend

trait EmailSender:
  def apply(email: EmailData): Unit

object EmailSender:
  def create(sttpBackend: SyncBackend, config: EmailConfig): EmailSender = if config.mailgun.enabled then
    new MailgunEmailSender(config.mailgun, sttpBackend)
  else if config.smtp.enabled then new SmtpEmailSender(config.smtp)
  else DummyEmailSender
