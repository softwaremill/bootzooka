package com.softwaremill.bootzooka.user

object RegisterDataValidator {
  val MinLoginLength = 3

  def isDataValid(login: String, email: String, password: String): Boolean =
    validLogin(login.trim) &&
      validEmail(email.trim) &&
      validPassword(password.trim)

  private def validLogin(login: String) =
    login.length >= MinLoginLength

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmail(email: String) = emailRegex.findFirstMatchIn(email).isDefined

  private def validPassword(password: String) = !password.isEmpty
}
