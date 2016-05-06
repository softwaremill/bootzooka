package com.softwaremill.bootzooka.user.worker

import akka.actor.{Actor, Props}
import com.softwaremill.bootzooka.user.{BasicUserData, User, UserDao, UserId}
import com.softwaremill.bootzooka.utils.{ActorPerRequest, ActorPerRequestFactory}
import com.softwaremill.bootzooka.utils.http.PerRequest._

import scala.concurrent.Future

class UserFinder(userDao: UserDao) extends ActorPerRequestFactory {

  override def props = Props(new UserFindActor)

  class UserFindActor extends Actor with ActorPerRequest {

    import UserFinder._
    import context.dispatcher

    implicit def userFoundTrans(future: Future[Option[User]]): Future[Event] = {
      future map {
        case Some(user) => UserFound(BasicUserData fromUser user)
        case None => UserNotFound
      }
    }

    def receive: Receive = {
      case FindUser(userId) =>
        pipeToSender(sender(), userDao findById userId)

      case FindUserByLowerCasedLogin(login) =>
        pipeToSender(sender(), userDao findByLowerCasedLogin login)

      case FindUserByEmail(email) =>
        pipeToSender(sender(), userDao findByEmail email)
    }

  }
}

object UserFinder {

  case class UserFound(msg: BasicUserData) extends OK
  case object UserNotFound extends NotFound

  case class FindUser(userId: UserId) extends Command
  case class FindUserByLowerCasedLogin(login: String) extends Command
  case class FindUserByEmail(email: String) extends Command

}

