package com.softwaremill.bootzooka.service.user

import org.apache.commons.validator.routines.EmailValidator

class RegistrationDataValidator() {

  def isDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean =
    validLogin(trim(loginOpt)) &&
    validEmail(trim(emailOpt)) &&
    validPassword(trim(passwordOpt))

  private def trim(s: Option[String]) = s map { _.trim }

  private def validLogin(loginOpt: Option[String]) =
    loginOpt exists { _.length >= RegistrationDataValidator.MinLoginLength }

  private def validEmail(emailOpt: Option[String]) =
    emailOpt filterNot { _.isEmpty } exists EmailValidator.getInstance().isValid

  private def validPassword(passwordOpt: Option[String]) =
    passwordOpt exists { !_.isEmpty }

}

object RegistrationDataValidator {
  val MinLoginLength = 3
}
