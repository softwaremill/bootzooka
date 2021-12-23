package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import com.softwaremill.bootzooka.test.BaseTest

import cats.effect.unsafe.implicits.global

class DummyEmailSenderTest extends BaseTest {
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content")).unsafeRunSync()
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
}
