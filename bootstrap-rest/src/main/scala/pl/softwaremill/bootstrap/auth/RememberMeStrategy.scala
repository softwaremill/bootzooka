package pl.softwaremill.bootstrap.auth

import org.scalatra.{CookieOptions, Cookie, CookieSupport, ScalatraBase}
import org.scalatra.auth.ScentryStrategy
import pl.softwaremill.bootstrap.common.Utils

class RememberMeStrategy(protected val app: ScalatraBase with CookieSupport, rememberMe: Boolean) extends ScentryStrategy[User] {

  private val CookieKey = "rememberMe"

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: User) {
    if (winningStrategy == name || (winningStrategy == UserPassword.name && rememberMe)) {
      val token = user.token
      app.response.addHeader("Set-Cookie",
        Cookie(CookieKey, token)(CookieOptions(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)).toCookieString)
    }
  }

  override def isValid = {
    app.cookies.get(CookieKey).flatMap(Some(_)).isDefined
  }

  override def authenticate() = {
    app.cookies.get(CookieKey).flatMap(Users.validateToken(_))
  }

  override def beforeLogout(user: User) {
    app.response.addHeader("Set-Cookie",
      Cookie(CookieKey, "")(CookieOptions(path = "/", secure = false, maxAge = 0, httpOnly = true)).toCookieString)
  }

}

object RememberMe {

  val name = "RememberMe"

}