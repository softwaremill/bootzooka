package pl.softwaremill.demo.spray

import akka.actor._
import spray.http._
import MediaTypes._
import HttpMethods._
import spray.json._

class UserService extends Actor with ActorLogging {

  def receive = {

    case HttpRequest(GET, "/", _, _, _) =>
      sender ! index

    case HttpRequest(GET, "/user", _, _, _) =>
      sender ! getUser

  }

  lazy val index = HttpResponse(
    entity = HttpBody(`text/html`,
      <html>
        <body>
          <h1>Say hello to <i>spray-servlet</i>!</h1>
          <p>Defined resources:</p>
          <ul>
            <li>
              <a href="/user">/user</a>
            </li>
          </ul>
        </body>
      </html>.toString())
  )

  case class User(name: String)

  object UserJsonProtocol extends DefaultJsonProtocol {
    implicit val userFormat = jsonFormat1(User)
  }

  lazy val getUser = {

    import UserJsonProtocol._

    val json = User("Lukasz").toJson

    HttpResponse(
      entity = HttpBody(`application/json`, json.toString())
    )

  }

}