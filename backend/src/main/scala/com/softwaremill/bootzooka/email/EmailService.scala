package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.IdGenerator
import ox.{Fork, IO, Ox, discard, forever, fork, sleep}

/** Schedules emails to be sent asynchronously, in the background, as well as manages sending of emails in batches. */
class EmailService(
    emailModel: EmailModel,
    idGenerator: IdGenerator,
    emailSender: EmailSender,
    config: EmailConfig,
    db: DB,
    metrics: Metrics
) extends EmailScheduler
    with Logging:

  def apply(data: EmailData)(using DbTx): Unit =
    logger.debug(s"Scheduling email to be sent to: ${data.recipient}")
    val id = idGenerator.nextId[Email]()
    emailModel.insert(Email(id, data))

  def sendBatch()(using IO): Unit =
    val emails = db.transact(emailModel.find(config.batchSize))
    if emails.nonEmpty then logger.info(s"Sending ${emails.size} emails")
    emails.map(_.data).foreach(emailSender.apply)
    db.transact(emailModel.delete(emails.map(_.id)))

  /** Starts an asynchronous process which attempts to send batches of emails in defined intervals, as well as updates a metric which holds
    * the size of the email queue.
    */
  def startProcesses()(using Ox, IO): Unit =
    foreverPeriodically("Exception when sending emails") {
      sendBatch()
    }

    foreverPeriodically("Exception when counting emails") {
      val count = db.transact(emailModel.count())
      metrics.emailQueueGauge.set(count.toDouble)
    }.discard

  private def foreverPeriodically(errorMsg: String)(t: => Unit)(using Ox): Fork[Nothing] =
    fork {
      forever {
        sleep(config.emailSendInterval)
        try t
        catch case e: Exception => logger.error(errorMsg, e)
      }
    }
end EmailService

trait EmailScheduler:
  def apply(data: EmailData)(using DbTx): Unit
