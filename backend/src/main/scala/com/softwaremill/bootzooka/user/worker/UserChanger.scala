package com.softwaremill.bootzooka.user.worker

import akka.actor.{Actor, Props}
import com.softwaremill.bootzooka.user.{User, UserDao, UserId}
import com.softwaremill.bootzooka.utils.{ActorPerRequest, ActorPerRequestFactory}
import com.softwaremill.bootzooka.utils.http.PerRequest._

import scala.concurrent.Future

class UserChanger(userDao: UserDao) extends ActorPerRequestFactory {

  override def props = Props(new UserChangeActor)

  class UserChangeActor extends Actor with ActorPerRequest {

    import UserChanger._
    import context.dispatcher

    def receive: Receive = {

      case ChangePassword(userId, currentPassword, newPassword) =>
        val f = userDao findById userId flatMap {
          case Some(user) if User.passwordsMatch(currentPassword, user) =>
            val pass = User.encryptPassword(newPassword, user.salt)
            userDao.changePassword(user.id, pass) map (_ => PasswordChanged)

          case Some(user) => Future successful PasswordIsInvalid

          case _ => Future successful UserIdIsInvalid
        }
        pipeToSender(sender(), f)

      case ChangeLogin(userId, newLogin) =>
        val f = userDao findByLowerCasedLogin newLogin flatMap {
          case None =>
            userDao.changeLogin(userId, newLogin) map (_ => LoginChanged)

          case _ => Future successful LoginIsTaken
        }
        pipeToSender(sender(), f)

      case ChangeEmail(userId, newEmail) =>
        val f = userDao findByEmail newEmail flatMap {
          case None =>
            userDao.changeEmail(userId, newEmail) map (_ => EmailChanged)

          case _ => Future successful EmailIsTaken
        }
        pipeToSender(sender(), f)
    }
  }

}

object UserChanger {

  case object PasswordChanged extends JustOK
  case object PasswordIsInvalid extends Forbidden("Current password is invalid")
  case object UserIdIsInvalid extends NotFound("User's id is invalid")

  case class ChangePassword(userId: UserId, currentPassword: String, newPassword: String) extends Command

  case object LoginChanged extends JustOK
  case object LoginIsTaken extends Forbidden("Login is already taken")
  case class LoginNotChanged(error: String) extends Forbidden(error)

  case class ChangeLogin(userId: UserId, login: String) extends Command

  case object EmailChanged extends JustOK
  case object EmailIsTaken extends Bad("E-mail used by another user")
  case class EmailNotChanged(error: String) extends Bad(error)

  case class ChangeEmail(userId: UserId, email: String) extends Command
}

