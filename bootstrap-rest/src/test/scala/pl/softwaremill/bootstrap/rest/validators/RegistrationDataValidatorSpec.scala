package pl.softwaremill.bootstrap.rest.validators

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class RegistrationDataValidatorSpec extends Specification with Mockito {

    val validator = new RegistrationDataValidator()


    "isDataValid()" should {

      "accept valid data" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), Some("password"))

        dataIsValid === true
      }

      "not accept missing login" in {
        val dataIsValid: Boolean = validator.isDataValid(None, Some("admin@sml.com"), Some("password"))

        dataIsValid === false
      }

      "not accept login containing only empty spaces" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("   "), Some("admin@sml.com"), Some("password"))

        dataIsValid === false
      }

      "not accept too short login" in {
        val tooShortLogin: String = "a" * (RegistrationDataValidator.MinLoginLength - 1)
        val dataIsValid: Boolean = validator.isDataValid(Some(tooShortLogin), Some("admin@sml.com"), Some("password"))

        dataIsValid === false
      }

      "not accept too short login after trimming" in {
        val loginTooShortAfterTrim: String = "a" * (RegistrationDataValidator.MinLoginLength - 1) + "   "
        val dataIsValid: Boolean = validator.isDataValid(Some(loginTooShortAfterTrim), Some("admin@sml.com"), Some("password"))

        dataIsValid === false
      }

      "not accept missing email" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), None, Some("password"))

        dataIsValid === false
      }

      "not accept missing email with spaces only" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("   "), Some("password"))

        dataIsValid === false
      }

      "not accept invalid email" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("invalidEmail"), Some("password"))

        dataIsValid === false
      }

      "not accept missing password" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), None)

        dataIsValid === false
      }

      "not accept password with empty spaces only" in {
        val dataIsValid: Boolean = validator.isDataValid(Some("login"), Some("admin@sml.com"), Some("    "))

        dataIsValid === false
      }

    }

}
