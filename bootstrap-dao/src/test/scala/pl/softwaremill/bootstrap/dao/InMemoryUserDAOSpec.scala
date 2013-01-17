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
  val user = User("login", "email", "pass", "token")

  def before {
    dao.users = List(user)
  }

  "InMemoryUserDAO" should {

    "change login of existing user" in {
      val newLogin = "newLogin"
      dao.changeLogin(user._id.toString, newLogin)
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
      dao.changeEmail(user._id.toString, newEmail)
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
