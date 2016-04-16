package com.softwaremill.bootzooka

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.config.{ServerConfig, CoreConfig}
import com.softwaremill.bootzooka.email.{EmailTemplatingEngine, EmailConfig, SmtpEmailService, DummyEmailService}
import com.softwaremill.bootzooka.passwordreset.{PasswordResetCodeDao, PasswordResetService}
import com.softwaremill.bootzooka.sql.{DatabaseConfig, SqlDatabase}
import com.softwaremill.bootzooka.user.rememberme.{RememberMeTokenDao, RefreshTokenStorageImpl}
import com.softwaremill.bootzooka.user.{UserDao, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

trait Beans extends StrictLogging {
  def system: ActorSystem

  lazy val config = new CoreConfig with EmailConfig with DatabaseConfig with ServerConfig {
    override def rootConfig = ConfigFactory.load()
  }

  lazy val daoExecutionContext = system.dispatchers.lookup("dao-dispatcher")

  lazy val userDao = new UserDao(sqlDatabase)(daoExecutionContext)

  lazy val codeDao = new PasswordResetCodeDao(sqlDatabase)(daoExecutionContext)

  lazy val rememberMeTokenDao = new RememberMeTokenDao(sqlDatabase)(daoExecutionContext)

  lazy val sqlDatabase = SqlDatabase.create(config)

  lazy val serviceExecutionContext = system.dispatchers.lookup("service-dispatcher")

  lazy val emailService = if (config.emailEnabled) {
    new SmtpEmailService(config)(serviceExecutionContext)
  }
  else {
    logger.info("Starting with fake email sending service. No emails will be sent.")
    new DummyEmailService
  }

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(
    userDao,
    emailService,
    emailTemplatingEngine
  )(serviceExecutionContext)

  lazy val passwordResetService = new PasswordResetService(
    userDao,
    codeDao,
    emailService,
    emailTemplatingEngine,
    config
  )(serviceExecutionContext)

  lazy val refreshTokenStorage = new RefreshTokenStorageImpl(rememberMeTokenDao, system)(serviceExecutionContext)
}
