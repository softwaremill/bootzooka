package pl.softwaremill.bootstrap.common

import Utils._
import org.specs2.mutable._

class UtilsTest extends Specification {

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

}
