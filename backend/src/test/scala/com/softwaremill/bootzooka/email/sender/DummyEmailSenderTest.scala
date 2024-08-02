package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import com.softwaremill.bootzooka.test.BaseTest

class DummyEmailSenderTest extends BaseTest:
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content"))
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
