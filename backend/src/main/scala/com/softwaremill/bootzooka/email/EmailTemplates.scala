package com.softwaremill.bootzooka.email

class EmailTemplates {
  def registrationConfirmation(userName: String): EmailSubjectContent = {
    EmailTemplateRenderer("registrationConfirmation", Map("userName" -> userName))
  }

  def passwordReset(userName: String, resetLink: String): EmailSubjectContent = {
    EmailTemplateRenderer("resetPassword", Map("userName" -> userName, "resetLink" -> resetLink))
  }

  def passwordChangeNotification(userName: String): EmailSubjectContent = {
    EmailTemplateRenderer("passwordChangeNotification", Map("userName" -> userName))
  }

  def profileDetailsChangeNotification(userName: String): EmailSubjectContent = {
    EmailTemplateRenderer("profileDetailsChangeNotification", Map("userName" -> userName))
  }
}
