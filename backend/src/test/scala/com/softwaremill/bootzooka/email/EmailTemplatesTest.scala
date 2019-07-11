package com.softwaremill.bootzooka.email

import org.scalatest.{FlatSpec, Matchers}

class EmailTemplatesTest extends FlatSpec with Matchers {
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
