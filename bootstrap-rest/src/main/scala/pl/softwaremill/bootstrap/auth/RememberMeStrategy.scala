package pl.softwaremill.bootstrap.auth

import org.scalatra.{CookieOptions, Cookie, CookieSupport, ScalatraBase}
import org.scalatra.auth.ScentryStrategy

class RememberMeStrategy(protected val app: ScalatraBase with CookieSupport, rememberMe: Boolean) extends ScentryStrategy[User] {

  val COOKIE_KEY = "rememberMe"
  private val oneWeek = 7 * 24 * 3600

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: User) {
    if (winningStrategy == name || (winningStrategy == UserPassword.name && rememberMe)) {
      val token = user.token
      app.response.addHeader("Set-Cookie",
        Cookie(COOKIE_KEY, token)(CookieOptions(path = "/", secure = false, maxAge = oneWeek, httpOnly = true)).toCookieString)
    }
  }

  override def authenticate() = {

    val token: String = app.cookies.get(COOKIE_KEY) match {
      case Some(v) => v
      case None => ""
    }

    Users.validateToken(token) match {
      case Some(user) => Option(user)
      case _ => None
    }

  }

  override def beforeLogout(user: User) {
    app.response.addHeader("Set-Cookie",
      Cookie(COOKIE_KEY, "")(CookieOptions(path = "/", secure = false, maxAge = 0, httpOnly = true)).toCookieString)
  }

}

object RememberMe {

  val name = "RememberMe"

}