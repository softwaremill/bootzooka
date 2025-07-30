package com.softwaremill.bootzooka.email

import com.augustnagro.magnum.{DbTx, PostgresDbType, Repo, Spec, SqlNameMapper, Table}
import com.softwaremill.bootzooka.infrastructure.Magnum.given
import com.softwaremill.bootzooka.util.Strings.Id
import ox.discard

/** Model for storing and retrieving scheduled emails. */
class EmailModel:
  private val emailRepo = Repo[ScheduledEmails, ScheduledEmails, Id[Email]]

  def insert(email: Email)(using DbTx): Unit = emailRepo.insert(ScheduledEmails(email))
  def find(limit: Int)(using DbTx): Vector[Email] = emailRepo.findAll(Spec[ScheduledEmails].limit(limit)).map(_.toEmail)
  def count()(using DbTx): Long = emailRepo.count
  def delete(ids: Vector[Id[Email]])(using DbTx): Unit = emailRepo.deleteAllById(ids).discard

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
private case class ScheduledEmails(id: Id[Email], recipient: String, subject: String, content: String):
  def toEmail: Email = Email(id, EmailData(recipient, EmailSubjectContent(subject, content)))

private object ScheduledEmails:
  def apply(email: Email): ScheduledEmails = ScheduledEmails(email.id, email.data.recipient, email.data.subject, email.data.content)

case class Email(id: Id[Email], data: EmailData)

case class EmailData(recipient: String, subject: String, content: String)
object EmailData:
  def apply(recipient: String, subjectContent: EmailSubjectContent): EmailData =
    EmailData(recipient, subjectContent.subject, subjectContent.content)

case class EmailSubjectContent(subject: String, content: String)
