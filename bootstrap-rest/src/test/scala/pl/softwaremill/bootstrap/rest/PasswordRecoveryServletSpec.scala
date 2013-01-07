package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.PasswordRecoveryService

/**
 * .
 */
class PasswordRecoveryServletSpec extends BootstrapServletSpec {
  def is = sequential ^ "PasswordRecoveryServlet" ^
    "POST to / should send e-mail to user" ! shouldGenerateNewCode

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
}
