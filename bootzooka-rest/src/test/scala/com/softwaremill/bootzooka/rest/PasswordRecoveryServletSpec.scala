package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.BootstrapServletSpec
import org.json4s.JsonDSL._
import com.softwaremill.bootzooka.service.PasswordRecoveryService
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.BDDMockito._
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
        verify(recoveryService).sendResetCodeToUser("abc")
      }
    }
  }

  "POST /123 with password" should "change it" in {
    onServletWithMocks { (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (200)
        verify(recoveryService).performPasswordReset("123", "validPassword")
      }
    }
  }

  "POST /123 without password" should "complain" in {
    onServletWithMocks { (recoveryService) =>
      post("/123", mapToJson(Map("password" -> "")), defaultJsonHeaders) {
        status should be (400)
        body should be ("{\"value\":\"missingpassword\"}")
        verify(recoveryService, never()).performPasswordReset(Matchers.eq("123"), anyString)
      }
    }
  }

  "POST /123 with password but without code" should "complain" in {
    onServletWithMocks { (recoveryService) =>
      given(recoveryService.performPasswordReset("123", "validPassword")) willReturn  Left("Error")
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (403)
        body should be ("{\"value\":\"Error\"}")
      }
    }
  }
}
