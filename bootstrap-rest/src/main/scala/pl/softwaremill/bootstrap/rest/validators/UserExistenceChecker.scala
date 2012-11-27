package pl.softwaremill.bootstrap.rest.validators

import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User

class UserExistenceChecker(userService: UserService) {

  def check(user: User): Either[String, Unit] = {
    var messageEither: Either[String, Unit] = Right(None)

    userService.findByLogin(user.login) foreach( _ => messageEither = Left("Login already in use!"))
    userService.findByEmail(user.email) foreach( _ => messageEither = Left("E-mail already in use!"))

    messageEither
  }


}
