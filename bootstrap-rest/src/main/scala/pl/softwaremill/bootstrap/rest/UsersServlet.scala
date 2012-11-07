package pl.softwaremill.bootstrap.rest

import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class UsersServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with AuthenticationSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
        contentType = formats("json")

    }

  override def login: String = (parsedBody \ "login").extract[String]
  override def password: String = (parsedBody \ "password").extract[String]
  override def rememberMe: Boolean = (parsedBody \ "rememberMe").extract[Boolean]

}

