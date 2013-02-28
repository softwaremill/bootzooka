package pl.softwaremill.bootstrap.service.schedulers

import org.scalatest.FunSpec
import pl.softwaremill.bootstrap.service.templates.EmailContentWithSubject
import org.scalatest.matchers.ShouldMatchers

class DummyEmailSendingServiceSpec extends FunSpec with ShouldMatchers {
  describe("Dummy email sending service") {
    it("send scheduled email") {
      val service = new DummyEmailSendingService
      service.scheduleEmail("test@sml.com", new EmailContentWithSubject("content", "subject"))
      service.run()
      service.wasEmailSent("test@sml.com", "subject") should be(true)
    }
  }
}
