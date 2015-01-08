package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.dao.{MongoPasswordResetCodeDAO, MongoUserDAO}
import com.softwaremill.bootzooka.service.PasswordRecoveryService
import com.softwaremill.bootzooka.service.config.{BootzookaConfig, EmailConfig}
import com.softwaremill.bootzooka.service.email.{DummyEmailSendingService, ProductionEmailSendingService}
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.softwaremill.bootzooka.service.user.{RegistrationDataValidator, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging

trait Beans extends LazyLogging {
  lazy val config = new BootzookaConfig with EmailConfig {
    override def rootConfig = ConfigFactory.load()
  }

  lazy val userDao = new MongoUserDAO

  lazy val codeDao = new MongoPasswordResetCodeDAO()

  lazy val emailScheduler = if (config.emailEnabled) {
    new ProductionEmailSendingService(config)
  } else {
    logger.info("Starting with fake email sending service. No emails will be sent.")
    new DummyEmailSendingService
  }

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(
    userDao,
    new RegistrationDataValidator(),
    emailScheduler,
    emailTemplatingEngine)

  lazy val passwordRecoveryService = new PasswordRecoveryService(
    userDao,
    codeDao,
    emailScheduler,
    emailTemplatingEngine,
    config)
}
