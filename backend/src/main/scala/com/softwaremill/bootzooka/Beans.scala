package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.common.RealTimeClock
import com.softwaremill.bootzooka.common.logging.bugsnag.BugsnagErrorReporter
import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.{DatabaseConfig, Daos}
import com.softwaremill.bootzooka.service.PasswordRecoveryService
import com.softwaremill.bootzooka.service.config.{CoreConfig, EmailConfig}
import com.softwaremill.bootzooka.service.email.{DummyEmailService, SmtpEmailService}
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.softwaremill.bootzooka.service.user.{RegistrationDataValidator, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait Beans extends LazyLogging with Daos {
  lazy val config = new CoreConfig with EmailConfig with DatabaseConfig {
    override def rootConfig = ConfigFactory.load()
  }

  override lazy val sqlDatabase = SqlDatabase.create(config)
  override implicit val ec: ExecutionContext = global

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
    new RegistrationDataValidator(),
    emailService,
    emailTemplatingEngine
  )

  lazy val passwordRecoveryService = new PasswordRecoveryService(
    userDao,
    codeDao,
    emailService,
    emailTemplatingEngine,
    config
  )

  lazy val errorReporter = BugsnagErrorReporter(config)

}
