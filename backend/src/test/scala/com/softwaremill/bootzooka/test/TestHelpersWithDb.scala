package com.softwaremill.bootzooka.test

import akka.dispatch.ExecutionContexts
import com.softwaremill.bootzooka.common.sql.SqlDatabase
import com.softwaremill.bootzooka.email.application.{DummyEmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.user.application.{UserDao, UserService}
import com.softwaremill.bootzooka.user.domain.User
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.ExecutionContext

trait TestHelpersWithDb extends TestHelpers with ScalaFutures {

  lazy val emailService = new DummyEmailService()
  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userDao = new UserDao(sqlDatabase)
  lazy val userService = new UserService(userDao, emailService, emailTemplatingEngine)

  implicit def executionContext: ExecutionContext
  def sqlDatabase: SqlDatabase

  def newRandomStoredUser(password: Option[String] = None): User = {
    val u = newRandomUser(password)
    userDao.add(u).futureValue
    u
  }
}
