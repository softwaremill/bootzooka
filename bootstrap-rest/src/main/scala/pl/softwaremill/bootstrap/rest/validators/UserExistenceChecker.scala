package pl.softwaremill.bootstrap.rest.validators

import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User

class UserExistenceChecker(userService: UserService) {

  def check(user: User): Option[String] = {
    var messageOpt: Option[String] = None

    userService.findByLogin(user.login) foreach( _ => messageOpt = Some("Login already in use!"))
    userService.findByEmail(user.email) foreach( _ => messageOpt = Some("E-mail already in use!"))

    messageOpt
  }

}
