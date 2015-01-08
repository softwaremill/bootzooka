package com.softwaremill.bootzooka.service.templates

import org.scalatest.{Matchers, FlatSpec}

class EmailTemplatingEngineSpec extends FlatSpec with Matchers {
  behavior of "splitToContentAndSubject"

  val engine = new EmailTemplatingEngine

  it should "throw exception on invalid template" in {
    intercept[Exception] {
      engine.splitToContentAndSubject("invalid template")
    }
  }

  it should "not throw exception on correct template" in {
    engine.splitToContentAndSubject("subect\nContent")
  }

  it should "split template into subject and content" in {
    // When
    val email = engine.splitToContentAndSubject("subject\nContent\nsecond line")

    // Then
    email.subject should be ("subject")
    email.content should be ("Content\nsecond line")
  }

  it should "generate the registration confirmation email" in {
    // when
    val email = engine.registrationConfirmation("adamw")

    // then
    email.subject should be ("SoftwareMill Bootzooka - registration confirmation for user adamw")
    email.content should include ("Dear adamw,")
    email.content should include ("Regards,")
  }
}
