package com.softwaremill.bootzooka.dao.user

import com.softwaremill.bootzooka.domain.User

import scala.concurrent.{ExecutionContext, Future}

class InMemoryUserDao(implicit val ec: ExecutionContext) extends UserDao {

  var users = List[User]()

  override def loadAll(): Future[List[User]] = {
    Future { users }
  }

  override def findForIdentifiers(uniqueIds: Set[UserId]): Future[List[User]] = {
    Future { users.filter(user => uniqueIds.contains(user.id))}
  }

  override protected def internalAddUser(user: User) = {
    Future { users ::= user }
  }

  override def remove(userId: UserId) = {
    Future {
      load(userId) foreach { user =>
        users = users.diff(List(user))
      }
    }
  }

  override def load(userId: UserId) = {
    Future {
      users.find(_.id == userId)
    }
  }

  override def findByEmail(email: String) = {
    Future {
      users.find(user => user.email.toLowerCase == email.toLowerCase)
    }
  }

  override def findByLowerCasedLogin(login: String) = {
    Future {
      users.find(user => user.loginLowerCased == login.toLowerCase)
    }
  }

  override def findByLoginOrEmail(loginOrEmail: String) = {
    findByLowerCasedLogin(loginOrEmail).flatMap(userOpt =>
      userOpt.map(user => Future{Some(user)}).getOrElse(findByEmail(loginOrEmail))
    )
  }

  def findByToken(token: String) = {
    Future {
      users.find(user => user.token == token)
    }
  }

  def changePassword(userId: UserId, password: String) = {
    load(userId).map { userOpt =>
      userOpt.foreach { user =>
        users = users.updated(users.indexOf(user), user.copy(password = password))
      }
    }
  }

  def changeLogin(currentLogin: String, newLogin: String) = {
    findByLowerCasedLogin(currentLogin).map { userOpt =>
      userOpt.foreach { user =>
        users = users.updated(users.indexOf(user), user.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
      }
    }
  }

  def changeEmail(currentEmail: String, newEmail: String) = {
    findByEmail(currentEmail).map { userOpt =>
      userOpt.foreach { user =>
        users = users.updated(users.indexOf(user), user.copy(email = newEmail))
      }
    }
  }
}
