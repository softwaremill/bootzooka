package pl.softwaremill.bootstrap.auth

import org.scalatra.{SweetCookies, ScalatraBase, CookieSupport}
import org.specs2.mock.Mockito
import javax.servlet.http.HttpServletResponse
import org.scalatra.test.specs2.MutableScalatraSpec
import pl.softwaremill.bootstrap.rest.EntriesServlet

// http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html

class RememberMeStrategySpec extends MutableScalatraSpec with Mockito {

  "RememberMe" should {
    val httpResponse = mock[HttpServletResponse]
    val app = mock[EntriesServlet]

    app.cookies returns new SweetCookies(Map(("rememberMe", User("admin", "admin").token)), httpResponse)

    val rememberMe = true
    val strategy = new RememberMeStrategy(app, rememberMe)
    val user: Option[User] = strategy.authenticate()

    "authenticate user base on cookie" in {
      user must not be equalTo(None)
      user.get.login must be equalTo ("admin")
    }
  }

}
