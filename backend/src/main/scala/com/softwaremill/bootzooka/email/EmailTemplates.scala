package com.softwaremill.bootzooka.email

class EmailTemplates {
  def registrationConfirmation(userName: String): EmailSubjectContent = {
    EmailTemplateRenderer("registrationConfirmation", Map("userName" -> userName))
  }

  def passwordReset(userName: String, resetLink: String): EmailSubjectContent = {
    EmailTemplateRenderer("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
  }
}
