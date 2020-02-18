package com.softwaremill.bootzooka.email

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EmailTemplatesTest extends AnyFlatSpec with Matchers {
  val templates = new EmailTemplates

  it should "generate the registration confirmation email" in {
    // when
    val email = templates.registrationConfirmation("john")

    // then
    email.subject should be("SoftwareMill Bootzooka - registration confirmation for user john")
    email.content should include("Dear john,")
    email.content should include("Regards,")
  }
}
