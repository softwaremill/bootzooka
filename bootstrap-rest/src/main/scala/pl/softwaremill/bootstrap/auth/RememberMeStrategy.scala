package pl.softwaremill.bootstrap.auth

import org.scalatra.{ CookieOptions, Cookie, CookieSupport, ScalatraBase }
import org.scalatra.auth.ScentryStrategy
import pl.softwaremill.bootstrap.common.Utils
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.data.UserJson

class RememberMeStrategy(protected val app: ScalatraBase with CookieSupport, rememberMe: Boolean, val userService: UserService) extends ScentryStrategy[UserJson] {

  private val CookieKey = "rememberMe"

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: UserJson) {
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
    app.cookies.get(CookieKey).flatMap(userService.authenticateWithToken(_))
  }

  override def beforeLogout(user: UserJson) {
    app.response.addHeader("Set-Cookie",
      Cookie(CookieKey, "")(CookieOptions(path = "/", secure = false, maxAge = 0, httpOnly = true)).toCookieString)
  }

}

object RememberMe {

  val name = "RememberMe"

}