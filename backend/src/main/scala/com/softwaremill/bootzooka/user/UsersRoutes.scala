package com.softwaremill.bootzooka.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.api.RoutesSupport
import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.session.SessionDirectives._
import com.typesafe.scalalogging.StrictLogging
import org.json4s._

import scala.concurrent.Future

trait UsersRoutes extends RoutesSupport with StrictLogging {

  def userService: UserService

  implicit val userJsonCbs = CanBeSerialized[UserJson]

  val usersRoutes = pathPrefix("users") {
    path("logout") {
      get {
        userIdFromSession { _ =>
          invalidatePersistentSession() {
            completeOk
          }
        }
      }
    } ~
      path("register") {
        post {
          entity(as[JValue]) { body =>
            val login = (body \ "login").extract[String]
            val loginEscaped = scala.xml.Utility.escape(login)
            val email = (body \ "email").extract[String]
            val password = (body \ "password").extract[String]

            onSuccess(userService.registerNewUser(loginEscaped, email, password)) {
              case UserRegisterResult.InvalidData => complete(StatusCodes.BadRequest, StringJsonWrapper("Wrong user data!"))
              case UserRegisterResult.UserExists(msg) => complete(StatusCodes.Conflict, StringJsonWrapper(msg))
              case UserRegisterResult.Success => complete(StringJsonWrapper("success"))
            }
          }
        }
      } ~
      path("changepassword") {
        post {
          userFromSession { user =>
            entity(as[JValue]) { body =>
              val currentPassword = (body \ "currentPassword").extract[String]
              val newPassword = (body \ "newPassword").extract[String]

              onSuccess(userService.changePassword(user.id, currentPassword, newPassword)) {
                case Left(msg) => complete(StatusCodes.Forbidden, StringJsonWrapper(msg))
                case Right(_) => completeOk
              }
            }
          }
        }
      } ~
      post {
        entity(as[JValue]) { body =>
          val login = (body \ "login").extract[String]
          val password = (body \ "password").extract[String]
          val rememberMe = (body \ "rememberMe").extractOpt[Boolean].getOrElse(false)

          onSuccess(userService.authenticate(login, password)) {
            case None => reject(AuthorizationFailedRejection)
            case Some(user) =>
              val session = Session(user.id)
              (if (rememberMe) {
                setPersistentSession(session)
              }
              else {
                setSession(session)
              }) { complete(user) }
          }
        }
      } ~
      get {
        userFromSession { user =>
          complete(user)
        }
      } ~
      patch {
        userIdFromSession { userId =>
          entity(as[JValue]) { body =>
            val loginOpt = (body \ "login").extractOpt[String]
            val emailOpt = (body \ "email").extractOpt[String]

            val updateAction = (loginOpt, emailOpt) match {
              case (Some(login), _) => userService.changeLogin(userId, login)
              case (_, Some(email)) => userService.changeEmail(userId, email)
              case _ => Future.successful(Left("You have to provide new login or email"))
            }

            onSuccess(updateAction) {
              case Left(msg) => complete(StatusCodes.Conflict, msg)
              case Right(_) => completeOk
            }
          }
        }
      }
  }
}
