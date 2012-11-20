package pl.softwaremill.bootstrap.common

import org.scalatest.FunSuite
import Utils._

class UtilsTest extends FunSuite {

  test("'true' converts to boolean true with checkbox()") {

    assert(checkbox("true"))

  }

  test("'tRuE' converts to boolean true with checkbox() ") {

    assert(checkbox("tRuE"))

  }

  test("pass null as a parameter to checkbox()") {

    assert(checkbox(null) == false)

  }

}
