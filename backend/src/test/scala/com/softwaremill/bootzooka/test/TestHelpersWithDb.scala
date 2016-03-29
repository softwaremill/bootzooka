package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.user.{User, UserDao}
import org.scalatest.concurrent.ScalaFutures

trait TestHelpersWithDb extends TestHelpers with ScalaFutures {

  def userDao: UserDao

  def newRandomStoredUser(password: Option[String] = None): User = {
    val u = newRandomUser(password)
    userDao.add(u).futureValue
    u
  }
}
