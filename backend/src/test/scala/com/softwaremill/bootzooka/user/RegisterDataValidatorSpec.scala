package com.softwaremill.bootzooka.user

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mock.MockitoSugar

class RegisterDataValidatorSpec extends FlatSpec with Matchers with MockitoSugar {

  "isDataValid()" should "accept valid data" in {
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid("login", "admin@sml.com", "password")

    dataIsValid should be (true)
  }

  "isDataValid()" should "not accept login containing only empty spaces" in {
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid("   ", "admin@sml.com", "password")

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept too short login" in {
    val tooShortLogin: String = "a" * (RegisterDataValidator.MinLoginLength - 1)
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid(tooShortLogin, "admin@sml.com", "password")

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept too short login after trimming" in {
    val loginTooShortAfterTrim: String = "a" * (RegisterDataValidator.MinLoginLength - 1) + "   "
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid(loginTooShortAfterTrim, "admin@sml.com", "password")

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept missing email with spaces only" in {
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid("login", "   ", "password")

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept invalid email" in {
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid("login", "invalidEmail", "password")

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept password with empty spaces only" in {
    val dataIsValid: Boolean = RegisterDataValidator.isDataValid("login", "admin@sml.com", "    ")

    dataIsValid should be (false)
  }
}
