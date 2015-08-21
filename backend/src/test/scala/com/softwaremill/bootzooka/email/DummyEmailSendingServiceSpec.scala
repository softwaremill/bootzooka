package com.softwaremill.bootzooka.email

import org.scalatest.{FunSpec, Matchers}

class DummyEmailSendingServiceSpec extends FunSpec with Matchers {
  describe("Dummy email sending service") {
    it("send scheduled email") {
      val service = new DummyEmailService
      service.scheduleEmail("test@sml.com", new EmailContentWithSubject("content", "subject"))
      service.wasEmailSent("test@sml.com", "subject") should be(true)
    }
  }
}
