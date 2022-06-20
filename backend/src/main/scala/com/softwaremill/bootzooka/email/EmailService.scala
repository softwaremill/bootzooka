package com.softwaremill.bootzooka.email

import cats.Parallel
import cats.effect.IO
import cats.syntax.all._
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.IdGenerator

/** Schedules emails to be sent asynchronously, in the background, as well as manages sending of emails in batches. */
class EmailService(emailModel: EmailModel, idGenerator: IdGenerator, emailSender: EmailSender, config: EmailConfig, xa: Transactor[IO])
    extends EmailScheduler
    with FLogging {

  def apply(data: EmailData): ConnectionIO[Unit] = {
    logger.debug[ConnectionIO](s"Scheduling email to be sent to: ${data.recipient}") >>
      idGenerator
        .nextId[ConnectionIO, Email]()
        .flatMap(id => emailModel.insert(Email(id, data)))
  }

  def sendBatch(): IO[Unit] = {
    for {
      emails <- emailModel.find(config.batchSize).transact(xa)
      _ <- if (emails.nonEmpty) logger.info[IO](s"Sending ${emails.size} emails") else ().pure[IO]
      _ <- emails.map(_.data).map(emailSender.apply).sequence
      _ <- emailModel.delete(emails.map(_.id)).transact(xa)
    } yield ()
  }

  /** Starts an asynchronous process which attempts to send batches of emails in defined intervals, as well as updates a metric which holds
    * the size of the email queue.
    */
  def startProcesses(): IO[(Nothing, Nothing)] = {
    val sendProcess = runForeverPeriodically("Exception when sending emails") {
      sendBatch()
    }

    val monitoringProcess = runForeverPeriodically("Exception when counting emails") {
      emailModel.count().transact(xa).map(_.toDouble).map(Metrics.emailQueueGauge.set)
    }

    Parallel.parProduct(sendProcess, monitoringProcess)
  }

  private def runForeverPeriodically[T](errorMsg: String)(t: IO[T]): IO[Nothing] = {
    (t >> IO.sleep(config.emailSendInterval))
      .onError(e => logger.error(errorMsg, e))
      .foreverM
  }
}

trait EmailScheduler {
  def apply(data: EmailData): ConnectionIO[Unit]
}
