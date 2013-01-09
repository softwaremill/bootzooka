package pl.softwaremill.bootstrap.service.templates

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate._
import java.io.File

class EmailTemplatingEngine {

  val TemplatesDirectory = "pl/softwaremill/bootstrap/service/templates/"

  val scalateEngine = new TemplateEngine(List(new File(TemplatesDirectory)), "production")

  def registrationConfirmation(userName: String): EmailContentWithSubject = {

    val template = prepareEmailTemplate("registrationConfirmation", Map("userName" -> userName))
    splitToContentAndSubject(template)
  }

  private def prepareEmailTemplate(templateNameWithoutExtension: String, params: Map[String, Object]): String = {
    scalateEngine.layout(TemplatesDirectory + templateNameWithoutExtension + ".mustache", params)
  }

  private[templates] def splitToContentAndSubject(template: String): EmailContentWithSubject = {
    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = template.split('\n')
    require(emailLines.length > 1, "Invalid email template. It should consist of at least two lines: one for subject and one for content")

    EmailContentWithSubject(emailLines.tail.mkString("\n"), emailLines.head)
  }

  def passwordReset(userName:String, resetLink:String) = {
    val template = prepareEmailTemplate("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
    splitToContentAndSubject(template)
  }

}
