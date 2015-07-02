package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.BaseServletSpec
import com.softwaremill.bootzooka.service.PasswordRecoveryService
import com.softwaremill.bootzooka.service.user.UserService
import org.json4s.JsonDSL._
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PasswordRecoveryServletSpec extends BaseServletSpec {

  def onServletWithMocks(testToExecute: (PasswordRecoveryService) => Unit) {
    val recoveryService = mock[PasswordRecoveryService]
    val userService = mock[UserService]
    when(userService.checkUserExistenceFor("existing", "existing")) thenReturn Future { Left("User exists") }
    when(userService.checkUserExistenceFor("notexisting", "notexisting")) thenReturn Future { Right(()) }

    val servlet = new PasswordRecoveryServlet(recoveryService, userService)
    addServlet(servlet, "/*")
    testToExecute(recoveryService)
  }

  "POST /" should "send e-mail to user" in {
    onServletWithMocks { (recoveryService) =>
      // given
      given(recoveryService.sendResetCodeToUser(any[String])).willReturn(Future.successful(()))
      // when
      post("/", mapToJson(Map("login" -> "existing")), defaultJsonHeaders) {
        status should be (200)
        body should be ("{\"value\":\"success\"}")
        verify(recoveryService).sendResetCodeToUser("existing")
      }
    }
  }

  "POST /" should "return error message when user not exists" in {
    onServletWithMocks { (recoveryService) =>
      post("/", mapToJson(Map("login" -> "notexisting")), defaultJsonHeaders) {
        status should be (404)
        verify(recoveryService, never()).sendResetCodeToUser("notexisting")
        body should be ("{\"value\":\"No user with given login/e-mail found.\"}")
      }
    }
  }

  "POST /123 with password" should "change it" in {
    onServletWithMocks { (recoveryService) =>
      // given
      given(recoveryService.performPasswordReset(any[String], any[String])).willReturn(Future{ Right(true) })
      // when
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (204)
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
      given(recoveryService.performPasswordReset("123", "validPassword")) willReturn Future { Left("Error") }
      post("/123", mapToJson(Map("password" -> "validPassword")), defaultJsonHeaders) {
        status should be (403)
        body should be ("{\"value\":\"Error\"}")
      }
    }
  }
}
