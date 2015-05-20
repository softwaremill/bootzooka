package com.softwaremill.bootzooka.auth

import com.softwaremill.bootzooka.rest.Halting
import org.scalatra._
import org.scalatra.auth.ScentryAuthStore.CookieAuthStore
import org.scalatra.auth.{ Scentry, ScentryConfig, ScentrySupport }
import com.softwaremill.bootzooka.common.{ Utils, StringJsonWrapper }
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.concurrent.Await
import AuthOps._

/**
 * It should be used with each servlet to support RememberMe functionality for whole application
 */
trait RememberMeSupport extends AuthenticationSupport {

  self: ScalatraBase with Halting =>

  before() {
    if (!isAuthenticated) {
      scentry.authenticate(RememberMe.name)
    }
  }

}

trait AuthenticationSupport extends ScentrySupport[UserJson] {

  self: ScalatraBase with Halting =>

  def userService: UserService

  override protected def registerAuthStrategies() {
    scentry.register(RememberMe.name, app => new RememberMeStrategy(app, rememberMe, userService))
    scentry.register(UserPassword.name, app => new UserPasswordStrategy(app, login, password, userService))
  }

  protected def fromSession = {
    case id: String =>
      val userFut = userService.findByLogin(id)
      // We have to block here since Scalatra does not talk well to async API
        val userOpt: Option[UserJson] = Await.result(userFut, SyncUserResolveTimeout)
      userOpt match {
        case Some(u) => u
        case _ => null
      }
  }

  protected def toSession = {
    case usr: UserJson => usr.login
  }

  override protected def configureScentry() {
    val authCookieOptions = cookieOptions.copy(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)
    scentry.store = new CookieAuthStore(self) {
      override def invalidate()(implicit request: HttpServletRequest, response: HttpServletResponse) {
        cookies.update(Scentry.scentryAuthKey, user.token)(authCookieOptions.copy(maxAge = 0))
      }
    }
    scentry.unauthenticated {
      Unauthorized(StringJsonWrapper("Unauthenticated"))
    }
  }

  // Define type to avoid casting as (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]
  type ScentryConfiguration = ScentryConfig

  protected def scentryConfig = {
    new ScentryConfig {}
  }

  /**
   * Implement to configure login process, must be only done on Login form
   */
  protected def login: String = ""

  protected def password: String = ""

  protected def rememberMe: Boolean = false

  def haltIfNotAuthenticated() {
    if (isAuthenticated == false) {
      haltWithUnauthorized("User not logged in")
    }
  }
}