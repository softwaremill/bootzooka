package com.softwaremill.bootzooka.email

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.tagging.@@
import cats.implicits._
import com.softwaremill.bootzooka.util.Id

/** Model for storing and retrieving scheduled emails.
  */
class EmailModel {

  def insert(email: Email): ConnectionIO[Unit] = {
    sql"""INSERT INTO scheduled_emails (id, recipient, subject, content)
         |VALUES (${email.id}, ${email.data.recipient}, ${email.data.subject}, ${email.data.content})""".stripMargin.update.run.void
  }

  def find(limit: Int): ConnectionIO[List[Email]] = {
    sql"SELECT id, recipient, subject, content FROM scheduled_emails LIMIT $limit"
      .query[Email]
      .to[List]
  }

  def count(): ConnectionIO[Int] = {
    sql"SELECT COUNT(*) FROM scheduled_emails"
      .query[Int]
      .unique
  }

  def delete(ids: List[Id @@ Email]): ConnectionIO[Unit] = {
    NonEmptyList.fromList(ids) match {
      case None    => ().pure[ConnectionIO]
      case Some(l) => (sql"DELETE FROM scheduled_emails WHERE " ++ Fragments.in(fr"id", l)).update.run.void
    }
  }
}

case class Email(id: Id @@ Email, data: EmailData)

case class EmailData(recipient: String, subject: String, content: String)
object EmailData {
  def apply(recipient: String, subjectContent: EmailSubjectContent): EmailData = {
    EmailData(recipient, subjectContent.subject, subjectContent.content)
  }
}

case class EmailSubjectContent(subject: String, content: String)
