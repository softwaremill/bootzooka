package com.softwaremill.bootzooka.user.worker

import com.softwaremill.bootzooka.test.{BaseActorPerRequestSpec, TestHelpersWithDb}
import com.softwaremill.bootzooka.user._
import com.softwaremill.bootzooka.user.worker.UserFinder._
import org.scalatest.Matchers

class UserFinderSpec extends BaseActorPerRequestSpec with Matchers with TestHelpersWithDb {

  val user = User.withRandomUUID("John", "newUser@sml.com", "password", "salt", createdOn)

  override protected def beforeEach() = {
    super.beforeEach()

    (userDao add user).futureValue
  }

  "findUser" should "find user by id, email and login" in {

    val userFound = UserFound(BasicUserData fromUser user)

    userFinder whenSend FindUser(user.id) thenExpect userFound

    userFinder whenSend FindUserByEmail(user.email) thenExpect userFound
    userFinder whenSend FindUserByEmail("anotherUser@sml.com") thenExpect UserNotFound

    userFinder whenSend FindUserByLowerCasedLogin(user.login.toLowerCase) thenExpect userFound
    userFinder whenSend FindUserByLowerCasedLogin(user.login) thenExpect userFound
    userFinder whenSend FindUserByLowerCasedLogin("anotherUser") thenExpect UserNotFound
  }
}
