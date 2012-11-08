package pl.softwaremill.bootstrap.auth

import org.scalatra._
import auth.{ScentryConfig, ScentrySupport}
import pl.softwaremill.bootstrap.common.JsonWrapper

trait AuthenticationSupport extends ScentrySupport[User] {

    self: ScalatraBase =>

    override protected def registerAuthStrategies {
        scentry.register("UserPassword", app => new UserPasswordStrategy(app, login, password))
        scentry.register("RememberMe", app => new RememberMeStrategy(app.asInstanceOf[ScalatraBase with CookieSupport], rememberMe))
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

    protected def scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

    /**
     * Override to configure login process, must be only done on Login form
     */
    protected def login: String = ""

    protected def password: String = ""

    protected def rememberMe: Boolean = false

    post() {
        val userOpt: Option[User] = authenticate()
        userOpt match {
            case Some(user) =>
                JsonWrapper(user)
            case _ =>
                halt(401, "Invalid login and/or password")
        }
    }

    get() {
        isAuthenticated match {
            case true => user
            case false => halt(401, "Not logged in!")
        }
    }

    get("/logout") {
        logOut()
    }

}