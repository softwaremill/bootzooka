package com.softwaremill.bootzooka.service.templates

import com.google.common.io.Resources
import java.nio.charset.Charset

class EmailTemplatingEngine {
  def registrationConfirmation(userName: String): EmailContentWithSubject = {
    val template = prepareEmailTemplate("registrationConfirmation", Map("userName" -> userName))
    addSignature(splitToContentAndSubject(template))
  }

  def passwordReset(userName:String, resetLink:String) = {
    val template = prepareEmailTemplate("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
    addSignature(splitToContentAndSubject(template))
  }

  private def prepareEmailTemplate(templateNameWithoutExtension: String, params: Map[String, Object]): String = {
    val rawTemplate = Resources.toString(
      Resources.getResource(this.getClass, s"/templates/email/$templateNameWithoutExtension.txt"),
      Charset.forName("UTF-8"))

    params.foldLeft(rawTemplate) { case (template, (param, paramValue)) =>
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
