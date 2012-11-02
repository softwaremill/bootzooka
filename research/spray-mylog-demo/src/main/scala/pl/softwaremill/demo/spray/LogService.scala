package pl.softwaremill.demo.spray

import akka.actor.Actor
import spray.json._
import spray.http._
import MediaTypes._
import spray.httpx.TwirlSupport
import spray.routing._
import html._
import net.liftweb.util.TimeHelpers

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class LogServiceActor extends Actor with LogService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}

trait LogService extends HttpService with TwirlSupport {

  import MyLogJsonProtocol._
  import DefaultJsonProtocol._

  def route = {
    path("") {
      get {
        complete(index(Entries.list))
      }
    } ~
      path("logs") {
        get {
          complete {
            Entries.list.map(_.toJson).toJson.toString()
          }
        }
      } ~
      path("log" / IntNumber) { id =>
          get {
            respondWithMediaType(`application/json`) {
              complete {
                Entries.list.find(_.id == id).get.toJson.toString()
              }
            }
          }
      } ~
      path("update") {
        post {
          formFields('author, 'text) { (author, text) =>
            Entries.list = (MyLog(Entries.list.length + 1, author, text, TimeHelpers.formattedDateNow)) :: Entries.list
            redirect("/")
          }
        }
      }
  }

}
