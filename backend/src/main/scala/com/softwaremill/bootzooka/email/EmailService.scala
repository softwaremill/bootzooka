package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.IdGenerator
import com.softwaremill.bootzooka.infrastructure.Doobie._
import monix.eval.{Fiber, Task}
import cats.implicits._
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.typesafe.scalalogging.StrictLogging

class EmailService(idGenerator: IdGenerator, emailSender: EmailSender, config: EmailConfig, xa: Transactor[Task])
    extends EmailScheduler
    with StrictLogging {

  def apply(data: EmailData): ConnectionIO[Unit] = EmailModel.insert(Email(idGenerator.nextId(), data))

  def sendBatch(): Task[Unit] = {
    for {
      emails <- EmailModel.find(config.batchSize).transact(xa)
      _ = if (emails.nonEmpty) logger.info(s"Sending ${emails.size} emails")
      _ <- Task.sequence(emails.map(_.data).map(emailSender.apply))
      _ <- EmailModel.delete(emails.map(_.id)).transact(xa)
    } yield ()
  }

  def startSender(): Task[Fiber[Nothing]] = {
    (sendBatch() >> Task.sleep(config.emailSendInterval))
      .onErrorHandle { e =>
        logger.error("Exception when sending emails", e)
      }
      .loopForever
      .start
  }
}

trait EmailScheduler {
  def apply(data: EmailData): ConnectionIO[Unit]
}
