package pl.softwaremill.bootstrap.auth

import org.scalatra._
import auth.{ScentryConfig, ScentrySupport}
import pl.softwaremill.bootstrap.common.JsonWrapper

trait AuthenticationSupport extends ScentrySupport[User] {

  self: ScalatraBase =>

  override protected def registerAuthStrategies {
    scentry.register(RememberMe.name, app => new RememberMeStrategy(app.asInstanceOf[ScalatraBase with CookieSupport], rememberMe))
    scentry.register(UserPassword.name, app => new UserPasswordStrategy(app, login, password))
  }

  protected def fromSession = {
    case id: String => {
      val userOpt: Option[User] = Users.list.find(_.login == id)
      userOpt match {
        case Some(u) => u
        case _ => null
      }
    }
  }

  protected def toSession = {
    case usr: User => usr.login
  }

  // Define type to avoid casting as (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]
  type ScentryConfiguration = ScentryConfig

  protected def scentryConfig = new ScentryConfig {}

  /**
   * Override to configure login process, must be only done on Login form
   */
  protected def login: String = ""

  protected def password: String = ""

  protected def rememberMe: Boolean = false

  before() {
    if (!isAuthenticated) {
      scentry.authenticate(RememberMe.name)
    }
  }

  post() {
    val userOpt: Option[User] = authenticate()
    userOpt match {
      case Some(user) =>
        user
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get() {
    haltIfNotAuthenticated()
    user
  }

  get("/logout") {
    logOut()
  }

  def haltIfNotAuthenticated() {
    if (isAuthenticated == false) {
      halt(401, "User not logged in")
    }
  }

  def haltWithForbiddenIf(f: Boolean) {
    if (f) halt(403, "Action forbidden")
  }

}