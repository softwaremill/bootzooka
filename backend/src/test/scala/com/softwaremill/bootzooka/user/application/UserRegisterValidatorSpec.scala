package com.softwaremill.bootzooka.user.application

import org.scalatest.{FlatSpec, Matchers}

class UserRegisterValidatorSpec extends FlatSpec with Matchers {

  "validate" should "accept valid data" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@sml.com", "password")

    dataIsValid should be (Right(()))
  }

  "validate" should "not accept login containing only empty spaces" in {
    val dataIsValid = UserRegisterValidator.validate("   ", "admin@sml.com", "password")

    dataIsValid should be ('left)
  }

  "validate" should "not accept too short login" in {
    val tooShortLogin = "a" * (UserRegisterValidator.MinLoginLength - 1)
    val dataIsValid = UserRegisterValidator.validate(tooShortLogin, "admin@sml.com", "password")

    dataIsValid should be ('left)
  }

  "validate" should "not accept too short login after trimming" in {
    val loginTooShortAfterTrim = "a" * (UserRegisterValidator.MinLoginLength - 1) + "   "
    val dataIsValid = UserRegisterValidator.validate(loginTooShortAfterTrim, "admin@sml.com", "password")

    dataIsValid should be ('left)
  }

  "validate" should "not accept missing email with spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "   ", "password")

    dataIsValid should be ('left)
  }

  "validate" should "not accept invalid email" in {
    val dataIsValid = UserRegisterValidator.validate("login", "invalidEmail", "password")

    dataIsValid should be ('left)
  }

  "validate" should "not accept password with empty spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@sml.com", "    ")

    dataIsValid should be ('left)
  }
}
