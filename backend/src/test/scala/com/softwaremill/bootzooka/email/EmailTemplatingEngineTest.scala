package com.softwaremill.bootzooka.email

import org.scalatest.{FlatSpec, Matchers}

class EmailTemplatingEngineTest extends FlatSpec with Matchers {
  behavior of "splitToContentAndSubject"

  val engine = new EmailTemplatingEngine

  it should "generate the registration confirmation email" in {
    // when
    val email = engine.registrationConfirmation("john")

    // then
    email.subject should be("SoftwareMill Bootzooka - registration confirmation for user john")
    email.content should include("Dear john,")
    email.content should include("Regards,")
  }
}
