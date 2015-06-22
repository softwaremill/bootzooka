package com.softwaremill.bootzooka.service.email.sender

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
