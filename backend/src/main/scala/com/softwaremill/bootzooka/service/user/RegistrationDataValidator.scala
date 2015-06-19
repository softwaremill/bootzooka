package com.softwaremill.bootzooka.service.user

class RegistrationDataValidator() {

  def isDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean =
    validLogin(trim(loginOpt)) &&
      validEmail(trim(emailOpt)) &&
      validPassword(trim(passwordOpt))

  private def trim(s: Option[String]) = s map { _.trim }

  private def validLogin(loginOpt: Option[String]) =
    loginOpt exists { _.length >= RegistrationDataValidator.MinLoginLength }

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmail(emailOpt: Option[String]) =
    emailOpt.filterNot(_.isEmpty).flatMap(emailRegex.findFirstMatchIn(_)).isDefined

  private def validPassword(passwordOpt: Option[String]) =
    passwordOpt exists { !_.isEmpty }

}

object RegistrationDataValidator {
  val MinLoginLength = 3
}
