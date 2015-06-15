package com.softwaremill.bootzooka.service.email

import org.scalatest.{Matchers, FunSpec}
import com.softwaremill.bootzooka.service.templates.EmailContentWithSubject
import scala.concurrent.ExecutionContext.Implicits.global

class DummyEmailSendingServiceSpec extends FunSpec with Matchers {
  describe("Dummy email sending service") {
    it("send scheduled email") {
      val service = new DummyEmailService
      service.scheduleEmail("test@sml.com", new EmailContentWithSubject("content", "subject"))
      service.wasEmailSent("test@sml.com", "subject") should be(true)
    }
  }
}
