package com.softwaremill.bootzooka.email

import scala.io.Source

object EmailTemplateRenderer {
  def apply(templateNameWithoutExtension: String, params: Map[String, String]): EmailSubjectContent = {
    val template = prepareTemplate(templateNameWithoutExtension, params)
    addSignature(splitToContentAndSubject(template))
  }

  private def prepareTemplate(templateNameWithoutExtension: String, params: Map[String, String]): String = {
    val source = Source
      .fromURL(getClass.getResource(s"/templates/email/$templateNameWithoutExtension.txt"), "UTF-8")

    try {
      val rawTemplate = source.getLines().mkString("\n")

      params.foldLeft(rawTemplate) { case (template, (param, paramValue)) =>
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

  private lazy val signature = prepareTemplate("emailSignature", Map())

  private def addSignature(email: EmailSubjectContent): EmailSubjectContent =
    email.copy(content = s"${email.content}\n$signature")
}
