package com.softwaremill.bootzooka.service.templates

import scala.io.Source

class EmailTemplatingEngine {
  def registrationConfirmation(userName: String): EmailContentWithSubject = {
    val template = prepareEmailTemplate("registrationConfirmation", Map("userName" -> userName))
    addSignature(splitToContentAndSubject(template))
  }

  def passwordReset(userName: String, resetLink: String) = {
    val template = prepareEmailTemplate("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
    addSignature(splitToContentAndSubject(template))
  }

  private def prepareEmailTemplate(templateNameWithoutExtension: String, params: Map[String, Object]): String = {
    val rawTemplate = Source
      .fromURL(getClass.getResource(s"/templates/email/$templateNameWithoutExtension.txt"), "UTF-8")
      .getLines()
      .mkString("\n")

    params.foldLeft(rawTemplate) {
      case (template, (param, paramValue)) =>
        template.replaceAll(s"\\{\\{$param\\}\\}", paramValue.toString)
    }
  }

  private[templates] def splitToContentAndSubject(template: String): EmailContentWithSubject = {
    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = template.split('\n')
    require(emailLines.length > 1, "Invalid email template. It should consist of at least two lines: one for subject and one for content")

    EmailContentWithSubject(emailLines.tail.mkString("\n"), emailLines.head)
  }

  private lazy val signature = prepareEmailTemplate("emailSignature", Map())

  private def addSignature(email: EmailContentWithSubject): EmailContentWithSubject = {
    email.copy(content = s"${email.content}\n$signature")
  }
}
