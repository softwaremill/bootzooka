package pl.softwaremill.bootstrap.auth

import org.scalatra.{CookieOptions, Cookie, CookieSupport, ScalatraBase}
import org.scalatra.auth.ScentryStrategy

class RememberMeStrategy(protected val app: ScalatraBase with CookieSupport, rememberMe: Boolean) extends ScentryStrategy[User] {

    val COOKIE_KEY = "rememberMe"
    private val oneWeek = 7 * 24 * 3600

    override def isValid = {
        app.cookies.get(COOKIE_KEY).isDefined
    }

    override def afterAuthenticate(winningStrategy: String, user: User) {
        if (winningStrategy == "RememberMe" || (winningStrategy == "UserPassword" && rememberMe)) {
            val token = user.token
            app.response.addHeader("Set-Cookie",
                Cookie(COOKIE_KEY, token)(CookieOptions(secure = false, maxAge = oneWeek, httpOnly = true)).toCookieString)
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
        app.cookies.get(COOKIE_KEY) foreach {
            _ => app.cookies.update(COOKIE_KEY, null)
        }
    }

}
