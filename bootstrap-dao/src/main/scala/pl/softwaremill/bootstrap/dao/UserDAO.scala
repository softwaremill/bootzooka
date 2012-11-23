package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils

class UserDAO {

  // simulates single table in database
  private var list = List(
    User(1, "Admin", "admin@admin.pl", Utils.sha256("admin", "Admin"))
  )

  var id: Int = 10

  private def nextId(): Int = {
    id = id + 1
    id
  }

  def loadAll = {
    list
  }

  def count(): Long = {
    list.size
  }

  def add(user: User) {
    val exists: Boolean = list.exists((u: User) => u.email.equalsIgnoreCase(user.email) || u.login.equalsIgnoreCase(user.login))
    if (exists) {
      throw new Exception("User with given e-mail or login already exists")
    }

    list = new User(nextId(), user.login, user.email, user.password) +: list
  }

  def remove(userId: Int) {
    val userOpt: Option[User] = list.find(_.id == userId)

    userOpt match {
      case Some(user) => list = list diff List(user)
      case _ => {}
    }
  }

  def load(userId: Int): Option[User] = {
    list.find(_.id == userId)
  }

  def findBy(p: User => Boolean) = {
    list.find(p)
  }

}
