package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

class InMemoryUserDAOSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter {
  behavior of "InMemoryUserDAO"

  var dao: InMemoryUserDAO = new InMemoryUserDAO
  val user = User("login", "email", "pass", "salt", "token")

  before {
    dao.users = List(user)
  }

  it should "change password of existing user" in {
    val encryptedNewPassword = User.encryptPassword("newPass", user.salt)
    dao.changePassword(user._id.toString, encryptedNewPassword)
    dao.load(user._id.toString) match {
      case Some(u) => u should be (user.copy(password = encryptedNewPassword))
      case None => fail("Couldn't load user")
    }
  }

  it should "change login of existing user" in {
    val newLogin = "newLogin"
    dao.changeLogin(user.login, newLogin)
    dao.findByLowerCasedLogin(newLogin) match {
      case Some(cu) => cu should be (user.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
      case None => fail("")
    }
  }

  it should "change email of existing user" in {
    val newEmail = "newEmail"
    dao.changeEmail(user.email, newEmail)
    dao.findByEmail(newEmail) match {
      case Some(cu) => cu should be (user.copy(email = newEmail))
      case None => fail("")
    }
  }
}
