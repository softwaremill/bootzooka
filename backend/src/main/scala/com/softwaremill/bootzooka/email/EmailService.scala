package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.infrastructure.Doobie._
import monix.eval.{Fiber, Task}
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.IdGenerator
import com.typesafe.scalalogging.StrictLogging

/**
  * Schedules emails to be sent asynchronously, in the background, as well as manages sending of emails in batches.
  */
class EmailService(emailModel: EmailModel, idGenerator: IdGenerator, emailSender: EmailSender, config: EmailConfig, xa: Transactor[Task])
    extends EmailScheduler
    with StrictLogging {

  def apply(data: EmailData): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling email to be sent to: ${data.recipient}")
    emailModel.insert(Email(idGenerator.nextId(), data))
  }

  def sendBatch(): Task[Unit] = {
    for {
      emails <- emailModel.find(config.batchSize).transact(xa)
      _ = if (emails.nonEmpty) logger.info(s"Sending ${emails.size} emails")
      _ <- Task.sequence(emails.map(_.data).map(emailSender.apply))
      _ <- emailModel.delete(emails.map(_.id)).transact(xa)
    } yield ()
  }

  /**
    * Starts an asynchronous process which attempts to send batches of emails in defined intervals, as well as updates
    * a metric which holds the size of the email queue.
    */
  def startProcesses(): Task[(Fiber[Nothing], Fiber[Nothing])] = {
    val sendProcess = runForeverPeriodically("Exception when sending emails") {
      sendBatch()
    }

    val monitoringProcess = runForeverPeriodically("Exception when counting emails") {
      emailModel.count().transact(xa).map(_.toDouble).map(Metrics.emailQueueGauge.set)
    }

    Task.parZip2(sendProcess, monitoringProcess)
  }

  private def runForeverPeriodically[T](errorMsg: String)(t: Task[T]): Task[Fiber[Nothing]] = {
    (t >> Task.sleep(config.emailSendInterval))
      .onErrorHandle { e =>
        logger.error(errorMsg, e)
      }
      .loopForever
      .start
  }
}

trait EmailScheduler {
  def apply(data: EmailData): ConnectionIO[Unit]
}
