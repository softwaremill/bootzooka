package pl.softwaremill.bootstrap.dao

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.domain.User
import org.specs2.specification.BeforeExample

/**
 * .
 */
class InMemoryUserDAOSpec extends Specification with BeforeExample {
  isolated
  var dao: InMemoryUserDAO = new InMemoryUserDAO
  val user = User("login", "email", "pass", "salt", "token")

  def before {
    dao.users = List(user)
  }

  "InMemoryUserDAO" should {

    "change password of existing user" in {
      val encryptedNewPassword = User.encryptPassword("newPass", user.salt)
      dao.changePassword(user._id.toString, encryptedNewPassword)
      dao.load(user._id.toString) match {
        case Some(u) => u must be equalTo user.copy(password = encryptedNewPassword)
        case None => failure("Couldn't load user")
      }
    }

    "change login of existing user" in {
      val newLogin = "newLogin"
      dao.changeLogin(user.login, newLogin)
      dao.findByLowerCasedLogin(newLogin) match {
        case Some(cu) => cu must be equalTo user.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase)
        case None => failure("")
      }
    }

    "change email of existing user" in {
      val newEmail = "newEmail"
      dao.changeEmail(user.email, newEmail)
      dao.findByEmail(newEmail) match {
        case Some(cu) => cu must be equalTo user.copy(email = newEmail)
        case None => failure("")
      }
    }
  }
}
