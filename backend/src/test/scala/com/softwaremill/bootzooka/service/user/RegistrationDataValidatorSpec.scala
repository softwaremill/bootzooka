package com.softwaremill.bootzooka.service.user

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

class RegistrationDataValidatorSpec extends FlatSpec with Matchers with MockitoSugar {
  val validator = new RegistrationDataValidator()

  "isDataValid()" should "accept valid data" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), Some("password"))

    dataIsValid should be (true)
  }

  "isDataValid()" should "not accept missing login" in {
    val dataIsValid: Boolean = validator.isDataValid(None, Some("admin@sml.com"), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept login containing only empty spaces" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("   "), Some("admin@sml.com"), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept too short login" in {
    val tooShortLogin: String = "a" * (RegistrationDataValidator.MinLoginLength - 1)
    val dataIsValid: Boolean = validator.isDataValid(Some(tooShortLogin), Some("admin@sml.com"), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept too short login after trimming" in {
    val loginTooShortAfterTrim: String = "a" * (RegistrationDataValidator.MinLoginLength - 1) + "   "
    val dataIsValid: Boolean = validator.isDataValid(Some(loginTooShortAfterTrim), Some("admin@sml.com"), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept missing email" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), None, Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept missing email with spaces only" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("   "), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept invalid email" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("invalidEmail"), Some("password"))

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept missing password" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), None)

    dataIsValid should be (false)
  }

  "isDataValid()" should "not accept password with empty spaces only" in {
    val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), Some("    "))

    dataIsValid should be (false)
  }
}
