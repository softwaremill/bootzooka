package com.softwaremill.bootzooka

import dao.{MongoFactory, InMemoryFactory}
import com.softwaremill.bootzooka.service.config.{EmailConfig, BootzookaConfig}
import service.PasswordRecoveryService
import service.email.{DummyEmailSendingService, ProductionEmailSendingService}
import service.templates.EmailTemplatingEngine
import service.user.{RegistrationDataValidator, UserService}
import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.ConfigFactory

trait Beans extends Logging {
  lazy val config = new BootzookaConfig with EmailConfig {
    override def rootConfig = ConfigFactory.load()
  }

  lazy val daoFactory = sys.props.get("withInMemory") match {
    case Some(value) => {
      logger.info("Starting with in-memory persistence")
      new InMemoryFactory
    }
    case None => new MongoFactory
  }

  lazy val emailScheduler = if (config.emailEnabled) {
    new ProductionEmailSendingService(config)
  } else {
    logger.info("Starting with fake email sending service. No emails will be sent.")
    new DummyEmailSendingService
  }

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(userDao, new RegistrationDataValidator(), emailScheduler, emailTemplatingEngine)

  lazy val userDao = daoFactory.userDAO

  lazy val passwordRecoveryService = new PasswordRecoveryService(userDao, daoFactory.codeDAO, emailScheduler,
    emailTemplatingEngine, config)
}
