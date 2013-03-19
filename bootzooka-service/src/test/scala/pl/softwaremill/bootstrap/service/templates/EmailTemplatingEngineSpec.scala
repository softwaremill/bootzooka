package pl.softwaremill.bootstrap.service.templates

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class EmailTemplatingEngineSpec extends FlatSpec with ShouldMatchers {
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
}
