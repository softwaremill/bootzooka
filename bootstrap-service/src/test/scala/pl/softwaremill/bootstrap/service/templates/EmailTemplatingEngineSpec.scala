package pl.softwaremill.bootstrap.service.templates

import org.specs2.mutable.Specification

class EmailTemplatingEngineSpec extends Specification {


  "splitToContentAndSubject" should {

    val engine = new EmailTemplatingEngine
    "throw exception on invalid template" in {
      engine.splitToContentAndSubject("invalid template") should throwA[IllegalArgumentException]
    }

    "not throw exception on correct template" in {
      engine.splitToContentAndSubject("subect\nContent") should(throwA[IllegalArgumentException]).not
    }

    "split template into subject and content" in {
      // When
      val email = engine.splitToContentAndSubject("subject\nContent\nsecond line")

      // Then
      email.subject shouldEqual("subject")
      email.content shouldEqual("Content\nsecond line")
    }
  }

}
