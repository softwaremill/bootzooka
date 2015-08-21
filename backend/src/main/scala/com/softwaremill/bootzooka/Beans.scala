package com.softwaremill.bootzooka

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.common.RealTimeClock
import com.softwaremill.bootzooka.common.logging.bugsnag.BugsnagErrorReporter
import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.{Daos, DatabaseConfig}
import com.softwaremill.bootzooka.service.PasswordResetService
import com.softwaremill.bootzooka.service.config.{ServerConfig, CoreConfig, EmailConfig}
import com.softwaremill.bootzooka.service.email.{DummyEmailService, SmtpEmailService}
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.softwaremill.bootzooka.service.user.{RememberMeStorageImpl, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

trait Beans extends StrictLogging with Daos {
  def system: ActorSystem

  lazy val config = new CoreConfig with EmailConfig with DatabaseConfig with ServerConfig {
    override def rootConfig = ConfigFactory.load()
  }

  override lazy val sqlDatabase = SqlDatabase.create(config)

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
