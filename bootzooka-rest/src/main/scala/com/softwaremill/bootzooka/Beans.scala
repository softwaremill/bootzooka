package com.softwaremill.bootzooka

import dao.{MongoFactory, InMemoryFactory}
import service.config.BootstrapConfiguration
import service.entry.EntryService
import service.PasswordRecoveryService
import service.schedulers.{DummyEmailSendingService, ProductionEmailSendingService}
import service.templates.EmailTemplatingEngine
import service.user.{RegistrationDataValidator, UserService}
import java.util.concurrent.Executors

trait Beans {
  lazy val scheduler = Executors.newScheduledThreadPool(4)

  lazy val daoFactory = sys.props.get("withInMemory") match {
    case Some(value) => new InMemoryFactory
    case None => new MongoFactory
  }

  lazy val emailSendingService = Option(BootstrapConfiguration.smtpHost) match {
    case Some(host) => new ProductionEmailSendingService
    case None => new DummyEmailSendingService
  }

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(userDao, new RegistrationDataValidator(), emailSendingService, emailTemplatingEngine)

  lazy val userDao = daoFactory.userDAO

  lazy val entryService = new EntryService(daoFactory.entryDAO, userDao)

  lazy val passwordRecoveryService = new PasswordRecoveryService(userDao, daoFactory.codeDAO, emailSendingService, emailTemplatingEngine)

}
