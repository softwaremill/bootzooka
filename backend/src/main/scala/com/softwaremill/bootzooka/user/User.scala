package com.softwaremill.bootzooka.user

import java.time.Instant

import com.softwaremill.bootzooka.{Id, LowerCased}
import com.softwaremill.tagging.@@
import tsec.common.VerificationStatus
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

case class User(
    id: Id @@ User,
    login: String,
    loginLowerCased: String @@ LowerCased,
    emailLowerCased: String @@ LowerCased,
    passwordHash: PasswordHash[SCrypt],
    createdOn: Instant
) {

  def verifyPassword(password: String): VerificationStatus = SCrypt.checkpw[cats.Id](password, passwordHash)
}

object User {
  def hashPassword(password: String): PasswordHash[SCrypt] = SCrypt.hashpw[cats.Id](password)
}
