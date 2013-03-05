package pl.softwaremill.bootstrap.domain

import com.mongodb.casbah.commons.TypeImports.ObjectId
import org.joda.time.DateTime

/**
 * Code used in the process of password reset.
 * By default this code has the `validTo` set to now plus 24 hours.
 */
case class PasswordResetCode(id: ObjectId, code: String, userId: ObjectId, validTo: DateTime)

object PasswordResetCode {
  def apply(code: String, userId: ObjectId) = {
    new PasswordResetCode(new ObjectId, code, userId, new DateTime().plusHours(24))
  }
}
