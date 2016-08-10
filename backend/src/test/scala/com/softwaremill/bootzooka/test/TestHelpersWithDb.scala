package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.common.sql.SqlDatabase
import com.softwaremill.bootzooka.email.application.{DummyEmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.user.application.{UserDao, UserService}
import com.softwaremill.bootzooka.user.domain.User
import org.scalatest.AsyncFlatSpec
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext

trait TestHelpersWithDb extends TestHelpers with ScalaFutures {

  this: AsyncFlatSpec =>

  lazy val emailService = new DummyEmailService()
  lazy val emailTemplatingEngine = new EmailTemplatingEngine
  lazy val initEc = ExecutionContext.global

  lazy val userDao = new UserDao(sqlDatabase)(initEc)
  lazy val userService = new UserService(userDao, emailService, emailTemplatingEngine)(initEc)

  def sqlDatabase: SqlDatabase

  def newRandomStoredUser(password: Option[String] = None): User = {
    val u = newRandomUser(password)
    userDao.add(u).futureValue
    u
  }
}
