package com.softwaremill.bootzooka.user

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserValidatorSpec extends AnyFlatSpec with Matchers:
  private def validate(userName: String, email: String, password: String) = validateUserData(Some(userName), Some(email), Some(password))

  "validate" should "accept valid data" in:
    val dataIsValid = validate("login", "admin@bootzooka.com", "password")

    dataIsValid shouldBe Right(())

  it should "not accept login containing only empty spaces" in:
    val dataIsValid = validate("   ", "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true

  it should "not accept too short login" in:
    val tooShortLogin = "a" * (MinLoginLength - 1)
    val dataIsValid = validate(tooShortLogin, "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true

  it should "not accept too short login after trimming" in:
    val loginTooShortAfterTrim = "a" * (MinLoginLength - 1) + "   "
    val dataIsValid = validate(loginTooShortAfterTrim, "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true

  it should "not accept missing email with spaces only" in:
    val dataIsValid = validate("login", "   ", "password")

    dataIsValid.isLeft shouldBe true

  it should "not accept invalid email" in:
    val dataIsValid = validate("login", "invalidEmail", "password")

    dataIsValid.isLeft shouldBe true

  it should "not accept password with empty spaces only" in:
    val dataIsValid = validate("login", "admin@bootzooka.com", "    ")

    dataIsValid.isLeft shouldBe true
end UserValidatorSpec
