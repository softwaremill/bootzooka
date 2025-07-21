package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.{EmailData, SmtpConfig}
import com.softwaremill.bootzooka.logging.Logging
import ox.discard

import java.util.{Date, Properties}
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Address, Message, Session, Transport}

/** Sends emails synchronously using SMTP. */
class SmtpEmailSender(config: SmtpConfig) extends EmailSender with Logging:
  def apply(email: EmailData): Unit =
    val emailToSend = new SmtpEmailSender.EmailDescription(List(email.recipient), email.content, email.subject)
    SmtpEmailSender.send(
      config.host,
      config.port,
      config.username,
      config.password.value,
      config.verifySslCertificate,
      config.sslConnection,
      config.from,
      config.encoding,
      emailToSend
    )
    logger.debug(s"Email: ${email.subject}, to: ${email.recipient}, sent")
  end apply
end SmtpEmailSender

/** Copied from softwaremill-common:
  * https://github.com/softwaremill/softwaremill-common/blob/master/softwaremill-sqs/src/main/java/com/softwaremill/common/sqs/email/EmailSender.java
  */
object SmtpEmailSender:
  def send(
      smtpHost: String,
      smtpPort: Int,
      smtpUsername: String,
      smtpPassword: String,
      verifySSLCertificate: Boolean,
      sslConnection: Boolean,
      from: String,
      encoding: String,
      emailDescription: EmailDescription
  ): Unit =
    val props = setupSmtpServerProperties(sslConnection, smtpHost, smtpPort, verifySSLCertificate)

    // Get a mail session
    val session = Session.getInstance(props)

    val m = new MimeMessage(session)
    m.setFrom(new InternetAddress(from))

    val to = convertStringEmailsToAddresses(emailDescription.emails)
    val replyTo = convertStringEmailsToAddresses(emailDescription.replyToEmails)
    val cc = convertStringEmailsToAddresses(emailDescription.ccEmails)
    val bcc = convertStringEmailsToAddresses(emailDescription.bccEmails)

    m.setRecipients(javax.mail.Message.RecipientType.TO, to)
    m.setRecipients(Message.RecipientType.CC, cc)
    m.setRecipients(Message.RecipientType.BCC, bcc)
    m.setReplyTo(replyTo)
    m.setSubject(emailDescription.subject, encoding)
    m.setSentDate(new Date())
    m.setText(emailDescription.message, encoding, "plain")

    val transport = createSmtpTransportFrom(session, sslConnection)
    try
      connectToSmtpServer(transport, smtpUsername, smtpPassword)
      sendEmail(transport, m)
    finally transport.close()
  end send

  private def setupSmtpServerProperties(
      sslConnection: Boolean,
      smtpHost: String,
      smtpPort: Int,
      verifySSLCertificate: Boolean
  ): Properties =
    // Setup mail server
    val props = new Properties()
    if sslConnection then
      props.put("mail.smtps.host", smtpHost)
      props.put("mail.smtps.port", smtpPort.toString)
      props.put("mail.smtps.starttls.enable", "true")
      if !verifySSLCertificate then
        props.put("mail.smtps.ssl.checkserveridentity", "false")
        props.put("mail.smtps.ssl.trust", "*").discard
    else
      props.put("mail.smtp.host", smtpHost)
      props.put("mail.smtp.port", smtpPort.toString)
    end if
    props
  end setupSmtpServerProperties

  private def createSmtpTransportFrom(session: Session, sslConnection: Boolean): Transport =
    if sslConnection then session.getTransport("smtps") else session.getTransport("smtp")

  private def sendEmail(transport: Transport, m: MimeMessage): Unit =
    transport.sendMessage(m, m.getAllRecipients)

  private def connectToSmtpServer(transport: Transport, smtpUsername: String, smtpPassword: String): Unit =
    if smtpUsername != null && smtpUsername.nonEmpty then transport.connect(smtpUsername, smtpPassword) else transport.connect()

  private def convertStringEmailsToAddresses(emails: Array[String]): Array[Address] =
    emails.map(new InternetAddress(_))

  case class EmailDescription(
      emails: Array[String],
      message: String,
      subject: String,
      replyToEmails: Array[String],
      ccEmails: Array[String],
      bccEmails: Array[String]
  ):
    def this(emails: List[String], message: String, subject: String) = this(emails.toArray, message, subject, Array(), Array(), Array())
  end EmailDescription
end SmtpEmailSender
