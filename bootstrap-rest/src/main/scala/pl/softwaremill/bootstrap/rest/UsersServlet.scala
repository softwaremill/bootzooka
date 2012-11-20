package pl.softwaremill.bootstrap.rest

import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class UsersServlet extends BootstrapServlet with CookieSupport with AuthenticationSupport {

  override def login: String = {
    (parsedBody \ "login").extractOpt[String] match {
      case Some(s) => s
      case _ => ""
    }
  }

  override def password: String = {
    (parsedBody \ "password").extractOpt[String] match {
      case Some(s) => s
      case _ => ""
    }
  }

  override def rememberMe: Boolean = {
    (parsedBody \ "rememberme").extractOpt[Boolean] match {
      case Some(b) => b
      case _ => false
    }
  }

}

