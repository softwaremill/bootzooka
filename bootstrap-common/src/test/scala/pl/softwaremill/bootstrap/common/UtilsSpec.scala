package pl.softwaremill.bootstrap.common

import Utils._
import org.specs2.mutable._

class UtilsSpec extends Specification {

  "checkbox()" should {
    "convert 'true' to boolean true" in {
      checkbox("true") === true
    }
    "convert 'tRuE' to boolean true" in {
      checkbox("tRuE") === true
    }
    "convert null to boolean false" in {
      checkbox(null) === false
    }
  }

  "sha1" should {
    "generate proper hash" in {
      sha1("admin") !== null
    }
    "generate string of length 40" in {
      sha1("admin").length === 40
    }
  }

}
