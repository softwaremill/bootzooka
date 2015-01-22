package com.softwaremill.bootzooka.domain

import java.util.UUID

import org.joda.time.DateTime

/**
 * Code used in the process of password reset.
 * By default this code has the `validTo` set to now plus 24 hours.
 */
case class PasswordResetCode(id: UUID, code: String, userId: UUID, validTo: DateTime)

/*
Extending function is a workaround for:
https://issues.scala-lang.org/browse/SI-3664
https://issues.scala-lang.org/browse/SI-4808
 */
object PasswordResetCode extends ((UUID, String, UUID, DateTime) => PasswordResetCode) {

  def apply(code: String, userId: UUID): PasswordResetCode =
    PasswordResetCode(UUID.randomUUID(), code, userId, new DateTime().plusHours(24))

}
