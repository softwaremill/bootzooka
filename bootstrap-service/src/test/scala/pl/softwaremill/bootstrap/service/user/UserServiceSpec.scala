package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.dao.UserDAO
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.User

class UserServiceSpec extends Specification with Mockito {

  def prepareMock: UserDAO = {
    val dao = mock[UserDAO]
    dao.findBy(any) returns Some(new User("admin", "admin@sml.pl", "pass"))
    dao
  }

  "findByEmail" should { // this test is silly :\
    "return user for admin@sml.pl" in {
      val userService = new UserService(prepareMock)

      val user: User = userService.findByEmail("admin@sml.pl").getOrElse(null)

      there was user !== null
      there was user.login === "admin"
      there was user.email === "admin@sml.pl"
    }
  }

}
