package pl.softwaremill.bootstrap.service.templates

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate._
import java.io.File

class EmailTemplatingEngine {

  val TemplatesDirectory = "pl/softwaremill/bootstrap/service/templates/"

  val scalateEngine = new TemplateEngine(List(new File("pl/softwaremill/bootstrap/service/templates/")), "production")

  def registrationConfirmation(userName: String):EmailContentWithSubject = {

    prepareEmailTemplate("registrationConfirmation", Map("userName" -> userName))
  }

  private def prepareEmailTemplate(templateNameWithoutExtension: String, params: Map[String, Object]):EmailContentWithSubject = {
    val contentWithSubject = scalateEngine.layout(TemplatesDirectory + templateNameWithoutExtension + ".mustache", params)

    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = contentWithSubject.split('\n')
    require(emailLines.length > 1, "Invalid email template. It should consist of at least two lines: one for subject and one for content")

    EmailContentWithSubject(emailLines.slice(1, emailLines.length).mkString("\n"), emailLines(0))
  }



}
