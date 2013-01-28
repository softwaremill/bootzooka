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
        case Some(u) => {
          (u.password must be equalTo encryptedNewPassword) and
            (u.token must be equalTo user.token) and
            (u.login must be equalTo user.login) and
            (u.email must be equalTo user.email) and
            (u.salt must be equalTo user.salt)
        }
        case None => failure("Couldn't load user")
      }
    }

    "change login of existing user" in {
      val newLogin = "newLogin"
      dao.changeLogin(user.login, newLogin)
      dao.findByLowerCasedLogin(newLogin) match {
        case Some(cu) => {
          cu._id === user._id and
            cu.login === newLogin and
            cu.loginLowerCased === newLogin.toLowerCase and
            cu.email === user.email and
            cu.password === user.password and
            cu.token === user.token
        }
        case None => failure("")
      }
    }

    "change email of existing user" in {
      val newEmail = "newEmail"
      dao.changeEmail(user.email, newEmail)
      dao.findByEmail(newEmail) match {
        case Some(cu) => {
          cu._id === user._id and
            cu.login === user.login and
            cu.loginLowerCased === user.loginLowerCased and
            cu.email === newEmail and
            cu.password === user.password and
            cu.token === user.token
        }
        case None => failure("")
      }
    }
  }
}
