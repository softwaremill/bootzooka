package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.{EmailData, MailgunConfig}
import com.softwaremill.sttp._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

class MailgunEmailSender(config: MailgunConfig)(implicit sttpBackend: SttpBackend[Task, Nothing]) extends EmailSender with StrictLogging {
  override def apply(email: EmailData): Task[Unit] = {
    sttp.auth
      .basic("api", config.apiKey)
      .post(uri"${config.url}")
      .body(
        Map(
          "from" -> s"${config.senderDisplayName} <${config.senderName}@${config.domain}>",
          "to" -> email.recipient,
          "subject" -> email.subject,
          "html" -> email.content
        )
      )
      .send()
      .map(_ => logger.debug(s"Email to: ${email.recipient} sent"))
  }
}
