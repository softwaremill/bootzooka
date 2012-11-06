package pl.softwaremill.bootstrap.rest

import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.common.{JsonWrapper}
import pl.softwaremill.bootstrap.common.JsonWrapper

class UsersServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
      contentType = formats("json")
    }

    post("/") {

      println("body =" + parsedBody)

      val login = (parsedBody \ "login").extract[String]
      println("login = " + login)
      println("password = " + (parsedBody \ "password").extract[String])
      println("rememberme = " + (parsedBody \ "rememberme").extract[Boolean])

      if(login.equalsIgnoreCase("admin")) {
        JsonWrapper("Johny Admin")
      }
      else {
        halt(401, "Invalid login and/or password");
      }
    }

    errorHandler = { case t => throw new RuntimeException("Something went terribly wrong") }
  }

