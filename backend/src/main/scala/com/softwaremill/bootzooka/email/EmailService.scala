package com.softwaremill.bootzooka.email

import cats.effect.{Fiber, IO}
import cats.free.Free
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.{Id, IdGenerator}
import com.typesafe.scalalogging.StrictLogging
import doobie.free.connection.ConnectionOp
import org.checkerframework.checker.units.qual.A
import cats.effect.unsafe.implicits.global
import com.softwaremill.bootzooka.infrastructure.Doobie
import com.softwaremill.tagging.@@

/** Schedules emails to be sent asynchronously, in the background, as well as manages sending of emails in batches.
  */
class EmailService(emailModel: EmailModel, idGenerator: IdGenerator, emailSender: EmailSender, config: EmailConfig, xa: Transactor[IO])
    extends EmailScheduler
    with StrictLogging {

  def apply(data: EmailData): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling email to be sent to: ${data.recipient}")
    idGenerator
      .nextId[Email]()
      .map(id => emailModel.insert(Email(id, data)))
      .unsafeRunSync()
  }

  def sendBatch(): IO[Unit] = {
    for {
      emails <- emailModel.find(config.batchSize).transact(xa)
      _ = if (emails.nonEmpty) logger.info(s"Sending ${emails.size} emails")
      _ <- IO(emails.map(_.data).map(emailSender.apply))
      _ <- emailModel.delete(emails.map(_.id)).transact(xa)
    } yield ()
  }

  /** Starts an asynchronous process which attempts to send batches of emails in defined intervals, as well as updates a metric which holds
    * the size of the email queue.
    */
  def startProcesses(): IO[(Fiber[IO, Throwable, Nothing], Fiber[IO, Throwable, Nothing])] = {
    val sendProcess = runForeverPeriodically("Exception when sending emails") {
      sendBatch()
    }

    val monitoringProcess = runForeverPeriodically("Exception when counting emails") {
      emailModel.count().transact(xa).map(_.toDouble).map(Metrics.emailQueueGauge.set)
    }

    sendProcess.flatMap(r => monitoringProcess.flatMap(d => IO(r, d)))
  }

  private def runForeverPeriodically[T](errorMsg: String)(t: IO[T]): IO[Fiber[IO, Throwable, Nothing]] = {
    (t >> IO.sleep(config.emailSendInterval))
      .onError { e =>
        IO(() => logger.error(errorMsg, e))
      }
      .foreverM
      .start
  }
}

trait EmailScheduler {
  def apply(data: EmailData): ConnectionIO[Unit]
}
