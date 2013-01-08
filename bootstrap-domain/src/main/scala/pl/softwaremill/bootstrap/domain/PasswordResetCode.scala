package pl.softwaremill.bootstrap.domain

import com.mongodb.casbah.commons.TypeImports.ObjectId
import org.joda.time.DateTime

/**
 * Code used in the process of password reset.
 * By default this code has the `validTo` set to now plus 24 hours.
 */
case class PasswordResetCode(var _id: ObjectId = new ObjectId, var code: String, var userId: ObjectId, var validTo: DateTime = new DateTime().plusHours(24))
