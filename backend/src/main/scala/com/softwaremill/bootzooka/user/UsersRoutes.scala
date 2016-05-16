package com.softwaremill.bootzooka.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.user.worker.{UserChanger, UserRegistrator}
import com.softwaremill.bootzooka.utils.http.RoutesSupport
import com.softwaremill.session.SessionDirectives._
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

import com.softwaremill.session.SessionOptions._

trait UsersRoutes extends RoutesSupport with StrictLogging {
  import UserChanger._
  import UserRegistrator._

  def userRegistrator: UserRegistrator
  def userChanger: UserChanger

  implicit val basicUserDataCbs = CanBeSerialized[BasicUserData]
  implicit val basicUserDataCbs1 = CanBeSerialized[User]

  case class ChangePasswordInput(currentPassword: String, newPassword: String) {
    def toChangePassword(userId: UserId) = ChangePassword(userId, currentPassword, newPassword)
  }

  case class LoginInput(login: String, password: String, rememberMe: Option[Boolean])

  case class PatchUserInput(login: Option[String], email: Option[String])

  val usersRoutes =
    pathPrefix("users") {
      path("logout") {
        get {
          userIdFromSession { _ =>
            invalidateSession(refreshable, usingCookies) {
              completeOk
            }
          }
        }
      } ~ path("register") {
        post {
          entity(as[RegisterUser]) { in =>
            perRequest(userRegistrator, in.withEscapedLogin) {
              case UserRegistered => complete("success")
            }
          }
        }
      } ~ path("changepassword") {
        post {
          userIdFromSession { userId =>
            entity(as[ChangePasswordInput]) { in =>
              perRequest(userChanger, in toChangePassword userId) {
                case PasswordChanged => completeOk
              }
            }
          }
        }
      } ~ pathEnd {
        post {
          entity(as[LoginInput]) { in =>

            perRequest(userRegistrator, AuthenticateUser(in.login, in.password)) {
              case UserAuthenticated(user) =>

                val session = Session(user.id)
                in.rememberMe orElse Some(false) map {
                  case remederMe if remederMe =>
                    setSession(refreshable, usingCookies, session)
                  case _ =>
                    setSession(oneOff, usingCookies, session)
                } map (_ apply complete(user)) getOrElse complete(StatusCodes.BadRequest)

              case UserRejected =>
                reject(AuthorizationFailedRejection)
            }
          }
        } ~ get {
          userFromSession { user =>
            complete(user)
          }
        } ~ patch {
          userIdFromSession { userId =>
            entity(as[PatchUserInput]) { in =>
              val updateAction = (in.login, in.email) match {
                case (Some(login), _) => perRequest(userChanger, ChangeLogin(userId, login)) _
                case (_, Some(email)) => perRequest(userChanger, ChangeEmail(userId, email)) _
                case _ => (_: Any) => complete(StatusCodes.Conflict, "You have to provide new login or email")
              }

              updateAction {
                case LoginChanged | EmailChanged => completeOk
              }
            }
          }
        }
      }
    }
}

