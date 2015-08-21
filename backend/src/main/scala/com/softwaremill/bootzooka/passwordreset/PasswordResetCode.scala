package com.softwaremill.bootzooka.passwordreset

import java.util.UUID
import com.softwaremill.bootzooka.user.User
import org.joda.time.DateTime

/**
 * Code used in the process of password reset.
 * By default this code has the `validTo` set to now plus 24 hours.
 */
case class PasswordResetCode(id: UUID, code: String, user: User, validTo: DateTime)

object PasswordResetCode {

  def apply(code: String, user: User): PasswordResetCode =
    PasswordResetCode(UUID.randomUUID(), code, user, new DateTime().plusHours(24))

}
