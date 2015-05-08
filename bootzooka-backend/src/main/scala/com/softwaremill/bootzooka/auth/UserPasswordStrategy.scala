
package com.softwaremill.bootzooka.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[UserJson] {

  override def name: String = UserPassword.name

  override def isValid(implicit request: HttpServletRequest) = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    Await.result(userService.authenticate(login, password), 1 second)
  }

}

object UserPassword {

  val name = "UserPassword"

}
