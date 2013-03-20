package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.BootzookaServletSpec
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue
import com.softwaremill.bootzooka.service.data.{EntriesWithTimeStamp, UserJson, EntryJson}
import com.softwaremill.bootzooka.service.entry.EntryService
import com.softwaremill.bootzooka.domain.User
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.BDDMockito._


class EntriesServletWithUserSpec extends BootzookaServletSpec {
  behavior of "EntriesServlet with logged in user"

  val loginJasKowalski = "JasKowalski"

  val entryOne = EntryJson("1", "Message from Jas", loginJasKowalski, "")
  val entryTwo = EntryJson("2", "Message 2 from Jas", loginJasKowalski, "")

  def onServletWithMocks(login: String, testToExecute: (EntryService, UserService) => Unit): Unit = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    when(entryService.count()) thenReturn 2
    when(entryService.loadAll) thenReturn EntriesWithTimeStamp(List(entryOne, entryTwo))
    when(entryService.load("1")) thenReturn Some(entryOne)
    when(entryService.load("2")) thenReturn Some(entryTwo)
    when(entryService.load("3")) thenReturn None
    when(entryService.isAuthor(loginJasKowalski, "1")) thenReturn true
    when(entryService.isAuthor(loginJasKowalski, "2")) thenReturn true

    val servlet: EntriesServlet = new EntriesServletWithUser(entryService, userService, login)
    addServlet(servlet, "/*")

    testToExecute(entryService, userService)
  }

  "POST /" should "modify entry that user owns" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "1", "text" -> "Important message")), defaultJsonHeaders) {
        verify(entryService).isAuthor(loginJasKowalski, "1")
        verify(entryService).update("1", "Important message")
        status should be (200)
      }
    )
  }

  "POST /" should "not update non existing entry" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "3", "text" -> "Important message")), defaultJsonHeaders) {
        verify(entryService).isAuthor(loginJasKowalski, "3")
        verify(entryService, never()).update(anyString, anyString)
        status should be (403)
      }
    )
  }

  "POST /" should "not update non owner entry" in {
    onServletWithMocks(login = "PiotrNowak", testToExecute = (entryService, usersService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "2", "text" -> "Important message")), defaultJsonHeaders) {
        verify(entryService).isAuthor("PiotrNowak", "2")
        verify(entryService, never()).update(anyString, anyString)
        status should be (403)
      }
    )
  }

  "PUT /" should "create new entry" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      post("/", mapToJson(Map("text" -> "New message")), defaultJsonHeaders) {
        verify(entryService).add(loginJasKowalski, "New message")
        verify(entryService, never()).update(anyString, anyString)
        status should be (200)
      }
    )
  }

  class EntriesServletWithUser(entryService: EntryService, userService: UserService, login: String) extends EntriesServlet(entryService, userService) {
    override def isAuthenticated(implicit request: javax.servlet.http.HttpServletRequest) = true
    override def user(implicit request: javax.servlet.http.HttpServletRequest) = new UserJson(login, "kowalski@kowalski.net", User.encryptPassword("password", "salt"))
  }
}
