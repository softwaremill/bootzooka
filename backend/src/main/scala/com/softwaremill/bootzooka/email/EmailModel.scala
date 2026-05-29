package com.softwaremill.bootzooka.email

import ma.chinespirit.parlance.{DbTx, EntityMeta, Postgres, QueryBuilder, Repo, SqlNameMapper, Table}
import com.softwaremill.bootzooka.infrastructure.Codecs.given
import com.softwaremill.bootzooka.util.Strings.Id
import ox.discard

/** Model for storing and retrieving scheduled emails. */
class EmailModel:
  private val emailRepo = Repo[ScheduledEmails, ScheduledEmails, Id[Email]]()

  def insert(email: Email)(using DbTx[Postgres]): Unit = emailRepo.rawInsert(ScheduledEmails(email))
  def find(limit: Int)(using DbTx[Postgres]): Vector[Email] = QueryBuilder.from[ScheduledEmails].limit(limit).run().map(_.toEmail)
  def count()(using DbTx[Postgres]): Long = emailRepo.count
  def delete(ids: Vector[Id[Email]])(using DbTx[Postgres]): Unit = emailRepo.deleteAllById(ids).discard

@Table(SqlNameMapper.CamelToSnakeCase)
private case class ScheduledEmails(id: Id[Email], recipient: String, subject: String, content: String) derives EntityMeta:
  def toEmail: Email = Email(id, EmailData(recipient, EmailSubjectContent(subject, content)))

private object ScheduledEmails:
  def apply(email: Email): ScheduledEmails = ScheduledEmails(email.id, email.data.recipient, email.data.subject, email.data.content)

case class Email(id: Id[Email], data: EmailData)

case class EmailData(recipient: String, subject: String, content: String)
object EmailData:
  def apply(recipient: String, subjectContent: EmailSubjectContent): EmailData =
    EmailData(recipient, subjectContent.subject, subjectContent.content)

case class EmailSubjectContent(subject: String, content: String)
