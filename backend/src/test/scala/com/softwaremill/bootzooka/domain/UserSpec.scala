package com.softwaremill.bootzooka.domain

import org.scalatest.{FlatSpec, Matchers}

class UserSpec extends FlatSpec with Matchers {
  "encrypt password" should "take into account the password" in {
    // given
    val p1 = "pass1"
    val p2 = "pass2"
    val salt = "salt"

    // when
    val e1 = User.encryptPassword(p1, salt)
    val e2 = User.encryptPassword(p2, salt)

    // then
    info(s"$p1 encrypted is: $e1")
    info(s"$p2 encrypted is: $e2")

    e1.length should be >= (10)
    e2.length should be >= (10)

    e1 should not be (e2)
  }

  "encrypt password" should "take into account the salt" in {
    // given
    val pass = "pass"
    val salt1 = "salt1"
    val salt2 = "salt2"

    // when
    val e1 = User.encryptPassword(pass, salt1)
    val e2 = User.encryptPassword(pass, salt2)

    // then
    info(s"$pass encrypted with $salt1 is: $e1")
    info(s"$pass encrypted with $salt2 is: $e2")

    e1.length should be >= (10)
    e2.length should be >= (10)

    e1 should not be (e2)
  }
}
