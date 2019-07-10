package com.softwaremill.bootzooka.email.sender

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.{Date, Properties}

import com.softwaremill.bootzooka.email.{EmailConfig, EmailData}
import com.typesafe.scalalogging.StrictLogging
import javax.activation.{DataHandler, DataSource}
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Address, Message, Session, Transport}
import monix.eval.Task

class SmtpEmailSender(config: EmailConfig) extends EmailSender with StrictLogging {
  def apply(email: EmailData): Task[Unit] = Task {
    val emailToSend = new SmtpEmailSender.EmailDescription(List(email.recipient), email.content, email.subject)
    SmtpEmailSender.send(
      config.smtp.host,
      config.smtp.port,
      config.smtp.username,
      config.smtp.password,
      config.smtp.verifySslCertificate,
      config.smtp.sslConnection,
      config.from,
      config.encoding,
      emailToSend
    )

    logger.debug(s"Email to: ${email.recipient} sent")
  }
}

/**
  * Copied from softwaremill-common:
  * https://github.com/softwaremill/softwaremill-common/blob/master/softwaremill-sqs/src/main/java/com/softwaremill/common/sqs/email/EmailSender.java
  */
object SmtpEmailSender extends StrictLogging {

  def send(
      smtpHost: String,
      smtpPort: Int,
      smtpUsername: String,
      smtpPassword: String,
      verifySSLCertificate: Boolean,
      sslConnection: Boolean,
      from: String,
      encoding: String,
      emailDescription: EmailDescription,
      attachmentDescriptions: AttachmentDescription*
  ): Unit = {

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

    if (attachmentDescriptions.nonEmpty) {
      addAttachments(m, emailDescription.message, encoding, attachmentDescriptions: _*)
    } else {
      m.setText(emailDescription.message, encoding, "plain")
    }

    val transport = createSmtpTransportFrom(session, sslConnection)
    try {
      connectToSmtpServer(transport, smtpUsername, smtpPassword)
      sendEmail(transport, m, emailDescription, to)
    } finally {
      transport.close()
    }
  }

  private def setupSmtpServerProperties(
      sslConnection: Boolean,
      smtpHost: String,
      smtpPort: Int,
      verifySSLCertificate: Boolean
  ): Properties = {
    // Setup mail server
    val props = new Properties()
    if (sslConnection) {
      props.put("mail.smtps.host", smtpHost)
      props.put("mail.smtps.port", smtpPort.toString)
      props.put("mail.smtps.starttls.enable", "true")
      if (!verifySSLCertificate) {
        props.put("mail.smtps.ssl.checkserveridentity", "false")
        props.put("mail.smtps.ssl.trust", "*")
      }
    } else {
      props.put("mail.smtp.host", smtpHost)
      props.put("mail.smtp.port", smtpPort.toString)
    }
    props
  }

  private def createSmtpTransportFrom(session: Session, sslConnection: Boolean): Transport =
    if (sslConnection) session.getTransport("smtps") else session.getTransport("smtp")

  private def sendEmail(transport: Transport, m: MimeMessage, emailDescription: EmailDescription, to: Array[Address]): Unit = {
    transport.sendMessage(m, m.getAllRecipients)
    logger.debug("Mail '" + emailDescription.subject + "' sent to: " + to.mkString(","))
  }

  private def connectToSmtpServer(transport: Transport, smtpUsername: String, smtpPassword: String): Unit = {
    if (smtpUsername != null && smtpUsername.nonEmpty) {
      transport.connect(smtpUsername, smtpPassword)
    } else {
      transport.connect()
    }
  }

  private def convertStringEmailsToAddresses(emails: Array[String]): Array[Address] =
    emails.map(new InternetAddress(_))

  private def addAttachments(
      mimeMessage: MimeMessage,
      msg: String,
      encoding: String,
      attachmentDescriptions: AttachmentDescription*
  ): Unit = {
    val multiPart = new MimeMultipart()

    val textPart = new MimeBodyPart()
    multiPart.addBodyPart(textPart)
    textPart.setText(msg, encoding, "plain")

    for (attachmentDescription <- attachmentDescriptions) {
      val binaryPart = new MimeBodyPart()
      multiPart.addBodyPart(binaryPart)

      val ds = new DataSource() {
        def getInputStream =
          new ByteArrayInputStream(attachmentDescription.content)

        def getOutputStream: ByteArrayOutputStream = {
          val byteStream = new ByteArrayOutputStream()
          byteStream.write(attachmentDescription.content)
          byteStream
        }

        def getContentType: String =
          attachmentDescription.contentType

        def getName: String =
          attachmentDescription.filename
      }
      binaryPart.setDataHandler(new DataHandler(ds))
      binaryPart.setFileName(attachmentDescription.filename)
      binaryPart.setDescription("")
    }

    mimeMessage.setContent(multiPart)
  }

  case class EmailDescription(
      emails: Array[String],
      message: String,
      subject: String,
      replyToEmails: Array[String],
      ccEmails: Array[String],
      bccEmails: Array[String]
  ) {

    def this(emails: List[String], message: String, subject: String) =
      this(emails.toArray, message, subject, Array(), Array(), Array())
  }

  case class AttachmentDescription(content: Array[Byte], filename: String, contentType: String)
}
