package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.PasswordRecoveryService
import org.mockito.Matchers

class PasswordRecoveryServletSpec extends BootstrapServletSpec {

  def onServletWithMocks(testToExecute: (PasswordRecoveryService) => Unit) {
    val recoveryService = mock[PasswordRecoveryService]
    val servlet = new PasswordRecoveryServlet(recoveryService)
    addServlet(servlet, "/*")
    testToExecute(recoveryService)
  }

  "POST /" should "send e-mail to user" in {
    onServletWithMocks { (recoveryService) =>
      post("/", mapToJson(Map("login" -> "abc")), defaultJsonHeaders) {
        status should be (200)
        there was one(recoveryService).sendResetCodeToUser("abc")
      }
    }
  }

  "POST /123 with password" should "change it" in {
    onServletWithMocks { (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (200)
        there was one(recoveryService).performPasswordReset("123", "validPassword")
      }
    }
  }

  "POST /123 without password" should "complain" in {
    onServletWithMocks { (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "")), defaultJsonHeaders) {
        status should be (400)
        body should be ("{\"value\":\"missingpassword\"}")
        there was no(recoveryService).performPasswordReset(Matchers.eq("123"), anyString)
      }
    }
  }

  "POST /123 with password but without code" should "complain" in {
    onServletWithMocks { (recoveryService) =>
      recoveryService.performPasswordReset("123", "validPassword") returns Left("Error")
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (403)
        body should be ("{\"value\":\"Error\"}")
      }
    }
  }
}
