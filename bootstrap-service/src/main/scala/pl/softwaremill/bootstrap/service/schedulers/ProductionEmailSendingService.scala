package pl.softwaremill.bootstrap.service.schedulers

import pl.softwaremill.common.sqs.email.EmailSender
import pl.softwaremill.bootstrap.service.config.BootstrapConfiguration._
import pl.softwaremill.common.sqs.util.EmailDescription
import javax.mail.MessagingException
import pl.softwaremill.common.sqs.{ReceivedMessage, Queue, SQS}
import com.google.common.base.Optional
import scala.util.control.Breaks._

class ProductionEmailSendingService extends EmailSendingService {

  val sqsClient = new SQS("queue.amazonaws.com", awsAccessKeyId, awsSecretAccessKey)

  def run() {
    var messageOpt: Optional[ReceivedMessage] = sqsClient.getQueueByName(taskSQSQueue).receiveSingleMessage

    breakable {
      logger.debug("Checking emails waiting in the Amazon SQS")

      while(messageOpt.isPresent) {
        val message: ReceivedMessage = messageOpt.get()
        val emailToSend: EmailDescription = message.getMessage.asInstanceOf[EmailDescription]

        try {
          EmailSender.send(smtpHost, smtpPort, smtpUserName, smtpPassword, from, encoding, emailToSend)
          logger.info("Email sent!")
          sqsClient.getQueueByName(taskSQSQueue).deleteMessage(message)
        }
        catch {
          case e: MessagingException => logger.error("Sending email failed: " + e.getMessage)
          break()
        }

        messageOpt = sqsClient.getQueueByName(taskSQSQueue).receiveSingleMessage
      }
    }
  }

  def scheduleEmail(address: String, subject: String, content: String) {
    val emailQueue: Queue = sqsClient.getQueueByName(taskSQSQueue)
    emailQueue.sendSerializable(new EmailDescription(address, content, subject))
  }
}
