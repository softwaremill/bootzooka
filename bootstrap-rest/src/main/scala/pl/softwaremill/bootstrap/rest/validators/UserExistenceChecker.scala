package pl.softwaremill.bootstrap.rest.validators

import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User

class UserExistenceChecker(userService: UserService) {

  def check(user: User): Option[String] = {
    var messageOpt: Option[String] = None

    userService.findByLogin(user.login) match {
      case Some(u) => messageOpt = Some("Login already in use!")
      case _ =>
    }

    userService.findByEmail(user.email) match {
      case Some(u) => messageOpt = Some("E-mail already in use!")
      case _ =>
    }

    messageOpt
  }

}
