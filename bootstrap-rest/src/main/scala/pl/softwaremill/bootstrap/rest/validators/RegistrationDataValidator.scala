package pl.softwaremill.bootstrap.rest.validators

import org.apache.commons.validator.routines.EmailValidator
import pl.softwaremill.bootstrap.service.UserService

class RegistrationDataValidator() {

  def isDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean = {

    loginOpt match {
      case Some(login) => if (login.trim.length < RegistrationDataValidator.minLoginLength) return false
      case _ => return false
    }

    emailOpt match {
      case Some(email) => if (email.trim.length == 0 || !EmailValidator.getInstance().isValid(email)) return false
      case _ => return false
    }

    passwordOpt match {
      case Some(password) => if (password.trim.length == 0) return false
      case _ =>  return false
    }

    return true
  }


}

object RegistrationDataValidator {
  val minLoginLength = 3
}
