package com.softwaremill.bootzooka.auth

import org.scalatra.{CookieOptions, Cookie, ScalatraBase}
import org.scalatra.auth.ScentryStrategy
import com.softwaremill.bootzooka.common.Utils
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.concurrent.Await
import AuthOps._

class RememberMeStrategy(protected val app: ScalatraBase, rememberMe: Boolean, val userService: UserService) extends ScentryStrategy[UserJson] {

  private val CookieKey = "rememberMe"

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: UserJson)(implicit request: HttpServletRequest, response: HttpServletResponse) {
    if (winningStrategy == name || (winningStrategy == UserPassword.name && rememberMe)) {
      val token = user.token
      app.response.addHeader(
        "Set-Cookie",
        Cookie(CookieKey, token)(CookieOptions(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)).toCookieString
      )
    }
  }

  override def isValid(implicit request: HttpServletRequest) = {
    app.cookies.get(CookieKey).flatMap(Some(_)).isDefined
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    app.cookies.get(CookieKey).flatMap(
      cookie => {
        val userFut = userService.authenticateWithToken(cookie)
        Await.result(userFut, SyncUserResolveTimeout)
      }
    )
  }

  override def beforeLogout(user: UserJson)(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.response.addHeader(
      "Set-Cookie",
      Cookie(CookieKey, "")(CookieOptions(path = "/", secure = false, maxAge = 0, httpOnly = true)).toCookieString
    )
  }

}

object RememberMe {

  val name = "RememberMe"

}