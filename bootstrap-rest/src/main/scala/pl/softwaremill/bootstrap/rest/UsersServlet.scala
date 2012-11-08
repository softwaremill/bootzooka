package pl.softwaremill.bootstrap.rest

import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class UsersServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with CookieSupport with AuthenticationSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
        contentType = formats("json")

    }

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

