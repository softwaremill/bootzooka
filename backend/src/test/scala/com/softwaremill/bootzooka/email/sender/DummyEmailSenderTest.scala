package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import com.softwaremill.bootzooka.test.BaseTest
import monix.execution.Scheduler.Implicits.global

class DummyEmailSenderTest extends BaseTest {
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content")).runSyncUnsafe()
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
}
