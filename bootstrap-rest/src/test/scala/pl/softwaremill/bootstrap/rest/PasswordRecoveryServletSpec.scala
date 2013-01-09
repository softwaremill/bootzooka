package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.PasswordRecoveryService
import org.mockito.Matchers

/**
 * .
 */
class PasswordRecoveryServletSpec extends BootstrapServletSpec {
  def is = sequential ^ "PasswordRecoveryServlet" ^
    "POST to / should send e-mail to user" ! shouldGenerateNewCode ^
    "POST to /123 with password should change it"  ! shouldChangePasswordWhenPasswordIsValid ^
    "POST to /123 without password should complain" ! shouldComplainWhenPasswordIsNotValid

  def onServletWithMocks(testToExecute: (PasswordRecoveryService) => MatchResult[Any]): MatchResult[Any] = {
    val recoveryService = mock[PasswordRecoveryService]
    val servlet = new PasswordRecoveryServlet(recoveryService)
    addServlet(servlet, "/*")
    testToExecute(recoveryService)
  }

  def shouldGenerateNewCode = onServletWithMocks {
    (recoveryService) =>
      post("/", mapToJson(Map("login" -> "abc")), defaultJsonHeaders) {
        status must beEqualTo(200)
        there was one(recoveryService).sendResetCodeToUser("abc")
      }
  }

  def shouldChangePasswordWhenPasswordIsValid = onServletWithMocks {
    (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status must beEqualTo(200)
        there was one(recoveryService).performPasswordReset("123", "validPassword")
      }
  }

  def shouldComplainWhenPasswordIsNotValid = onServletWithMocks {
    (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "")), defaultJsonHeaders) {
        status must beEqualTo(400)
        there was no(recoveryService).performPasswordReset(Matchers.eq("123"), anyString)
      }
  }
}
