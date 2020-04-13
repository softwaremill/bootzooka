package com.softwaremill.bootzooka.user

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserRegisterValidatorSpec extends AnyFlatSpec with Matchers {
  "validate" should "accept valid data" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@bootzooka.com", "password")

    dataIsValid shouldBe Right(())
  }

  "validate" should "not accept login containing only empty spaces" in {
    val dataIsValid = UserRegisterValidator.validate("   ", "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept too short login" in {
    val tooShortLogin = "a" * (UserRegisterValidator.MinLoginLength - 1)
    val dataIsValid   = UserRegisterValidator.validate(tooShortLogin, "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept too short login after trimming" in {
    val loginTooShortAfterTrim = "a" * (UserRegisterValidator.MinLoginLength - 1) + "   "
    val dataIsValid            = UserRegisterValidator.validate(loginTooShortAfterTrim, "admin@bootzooka.com", "password")

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept missing email with spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "   ", "password")

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept invalid email" in {
    val dataIsValid = UserRegisterValidator.validate("login", "invalidEmail", "password")

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept password with empty spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@bootzooka.com", "    ")

    dataIsValid.isLeft shouldBe true
  }
}
