package com.softwaremill.bootzooka

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.common.RealTimeClock
import com.softwaremill.bootzooka.common.logging.bugsnag.BugsnagErrorReporter
import com.softwaremill.bootzooka.config.{ServerConfig, CoreConfig}
import com.softwaremill.bootzooka.email.{EmailTemplatingEngine, EmailConfig, SmtpEmailService, DummyEmailService}
import com.softwaremill.bootzooka.passwordreset.{PasswordResetCodeDao, PasswordResetService}
import com.softwaremill.bootzooka.sql.{DatabaseConfig, SqlDatabase}
import com.softwaremill.bootzooka.user.rememberme.{RememberMeTokenDao, RememberMeStorageImpl}
import com.softwaremill.bootzooka.user.{UserDao, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

trait Beans extends StrictLogging {
  def system: ActorSystem
  implicit def ec: ExecutionContext

  lazy val config = new CoreConfig with EmailConfig with DatabaseConfig with ServerConfig {
    override def rootConfig = ConfigFactory.load()
  }

  lazy val userDao = new UserDao(sqlDatabase)

  lazy val codeDao = new PasswordResetCodeDao(sqlDatabase)

  lazy val rememberMeTokenDao = new RememberMeTokenDao(sqlDatabase)

  lazy val sqlDatabase = SqlDatabase.create(config)

  lazy val emailService = if (config.emailEnabled) {
    new SmtpEmailService(config)
  }
  else {
    logger.info("Starting with fake email sending service. No emails will be sent.")
    new DummyEmailService
  }

  implicit lazy val clock = RealTimeClock

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(
    userDao,
    emailService,
    emailTemplatingEngine
  )

  lazy val passwordResetService = new PasswordResetService(
    userDao,
    codeDao,
    emailService,
    emailTemplatingEngine,
    config
  )

  lazy val errorReporter = BugsnagErrorReporter(config)

  lazy val rememberMeStorage = new RememberMeStorageImpl(rememberMeTokenDao, system)
}
