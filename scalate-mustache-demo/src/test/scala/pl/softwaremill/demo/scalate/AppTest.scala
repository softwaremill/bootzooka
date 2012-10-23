package pl.softwaremill.demo.scalate

import org.fusesource.scalate.test.{WebDriverMixin, WebServerMixin}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

/**
 * Unit test for My Ap
 */
@RunWith(classOf[JUnitRunner])
class AppTest extends FunSuite with WebServerMixin with WebDriverMixin {

  ignore("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  //
  // TODO here is a sample test case for a page
  //
  // testPageContains("foo.scaml", "this is some content I expect")
}
