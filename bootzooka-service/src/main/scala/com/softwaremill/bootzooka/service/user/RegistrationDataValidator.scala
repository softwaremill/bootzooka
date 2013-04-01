package com.softwaremill.bootzooka.service.user

import org.apache.commons.validator.routines.EmailValidator

class RegistrationDataValidator() {

  def isDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean =
    validLogin(trim(loginOpt)) &&
    validEmail(trim(emailOpt)) &&
    validPassword(trim(passwordOpt))

  private def trim(s: Option[String]) = s map {_.trim}

  private def validLogin(loginOpt: Option[String]) =
    loginOpt map {_.length >= RegistrationDataValidator.MinLoginLength} getOrElse false

  private def validEmail(emailOpt: Option[String]) =
    emailOpt filterNot {_.isEmpty} map EmailValidator.getInstance().isValid getOrElse false

  private def validPassword(passwordOpt: Option[String]) =
    passwordOpt map {!_.isEmpty} getOrElse false

}

object RegistrationDataValidator {
  val MinLoginLength = 3
}
