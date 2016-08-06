package com.softwaremill.bootzooka.user.application

import java.time.OffsetDateTime
import java.util.UUID

import com.softwaremill.bootzooka.common.FutureHelpers._
import com.softwaremill.bootzooka.common.sql.SqlDatabase
import com.softwaremill.bootzooka.user.domain.RememberMeToken

import scala.concurrent.{ExecutionContext, Future}

class RememberMeTokenDao(protected val database: SqlDatabase)(implicit ec: ExecutionContext) extends SqlRememberMeSchema {

  import database._
  import database.driver.api._

  def findBySelector(selector: String): Future[Option[RememberMeToken]] =
    db.run(rememberMeTokens.filter(_.selector === selector).result).map(_.headOption)

  def add(data: RememberMeToken): Future[Unit] =
    db.run(rememberMeTokens += data).mapToUnit

  def remove(selector: String): Future[Unit] =
    db.run(rememberMeTokens.filter(_.selector === selector).delete).mapToUnit
}

trait SqlRememberMeSchema {
  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val rememberMeTokens = TableQuery[RememberMeTokens]

  protected class RememberMeTokens(tag: Tag) extends Table[RememberMeToken](tag, "remember_me_tokens") {
    def id = column[UUID]("id", O.PrimaryKey)
    def selector = column[String]("selector")
    def tokenHash = column[String]("token_hash")
    def userId = column[UUID]("user_id")
    def validTo = column[OffsetDateTime]("valid_to")

    def * = (id, selector, tokenHash, userId, validTo) <> (RememberMeToken.tupled, RememberMeToken.unapply)
  }
}
