package com.softwaremill.bootzooka.email.sender

import cats.effect.IO
import com.softwaremill.bootzooka.email.{EmailData, MailgunConfig}
import com.softwaremill.bootzooka.logging.FLogging
import sttp.client3._

/** Sends emails using the [[https://www.mailgun.com Mailgun]] service. The external http call is done using
  * [[sttp https://github.com/softwaremill/sttp]].
  */
class MailgunEmailSender(config: MailgunConfig, sttpBackend: SttpBackend[IO, Any]) extends EmailSender with FLogging {
  override def apply(email: EmailData): IO[Unit] = {
    basicRequest.auth
      .basic("api", config.apiKey.value)
      .post(uri"${config.url}")
      .body(
        Map(
          "from" -> s"${config.senderDisplayName} <${config.senderName}@${config.domain}>",
          "to" -> email.recipient,
          "subject" -> email.subject,
          "html" -> email.content
        )
      )
      .send(sttpBackend)
      .flatMap(_ => logger.debug(s"Email to: ${email.recipient} sent"))
  }
}
