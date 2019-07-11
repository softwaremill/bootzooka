package com.softwaremill.bootzooka.email

import scala.io.Source

class EmailTemplates {
  def registrationConfirmation(userName: String): EmailSubjectContent = {
    val template = prepareEmailTemplate("registrationConfirmation", Map("userName" -> userName))
    addSignature(splitToContentAndSubject(template))
  }

  def passwordReset(userName: String, resetLink: String): EmailSubjectContent = {
    val template = prepareEmailTemplate("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
    addSignature(splitToContentAndSubject(template))
  }

  private def prepareEmailTemplate(templateNameWithoutExtension: String, params: Map[String, Object]): String = {
    val source = Source
      .fromURL(getClass.getResource(s"/templates/email/$templateNameWithoutExtension.txt"), "UTF-8")

    try {
      val rawTemplate = source.getLines().mkString("\n")

      params.foldLeft(rawTemplate) {
        case (template, (param, paramValue)) =>
          template.replaceAll(s"\\{\\{$param\\}\\}", paramValue.toString)
      }
    } finally {
      source.close()
    }
  }

  private def splitToContentAndSubject(template: String): EmailSubjectContent = {
    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = template.split('\n')
    require(
      emailLines.length > 1,
      "Invalid email template. It should consist of at least two lines: one for subject and one for content"
    )

    EmailSubjectContent(emailLines.head, emailLines.tail.mkString("\n"))
  }

  private lazy val signature = prepareEmailTemplate("emailSignature", Map())

  private def addSignature(email: EmailSubjectContent): EmailSubjectContent =
    email.copy(content = s"${email.content}\n$signature")
}
