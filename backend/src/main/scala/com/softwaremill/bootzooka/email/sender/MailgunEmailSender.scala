package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.{EmailData, MailgunConfig}
import com.softwaremill.bootzooka.logging.Logging
import sttp.client4.*
import ox.discard

/** Sends emails using the [[https://www.mailgun.com Mailgun]] service. The external http call is done using
  * [[sttp https://github.com/softwaremill/sttp]].
  */
class MailgunEmailSender(config: MailgunConfig, sttpBackend: SyncBackend) extends EmailSender with Logging:
  override def apply(email: EmailData): Unit =
    basicRequest.auth
      .basic("api", config.apiKey.value)
      .post(uri"${config.url}")
      .response(asStringOrFail)
      .body(
        Map(
          "from" -> s"${config.senderDisplayName} <${config.senderName}@${config.domain}>",
          "to" -> email.recipient,
          "subject" -> email.subject,
          "html" -> email.content
        )
      )
      .send(sttpBackend)
      .discard
    logger.debug(s"Email to: ${email.recipient} sent")
