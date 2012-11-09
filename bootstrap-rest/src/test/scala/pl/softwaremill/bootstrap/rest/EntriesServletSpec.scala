package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class EntriesServletSpec extends ScalatraSpec {

    def is =
        "GET / on EntriesServlet" ^
            "should return status 200" ! root200 ^
            "should return content-type application/json" ! contentJson ^
            "should return JSON entries" ! jsonEntries add
        "GET /count on EntriesServlet" ^
            "should return number of entries" ! countEntries

    end

    addServlet(classOf[EntriesServlet], "/*")

    def root200 = get("/") {
        status must_== 200
    }

    def contentJson = get("/") {
        header.get("Content-Type").get contains "application/json"
    }

    def jsonEntries = get("/") {
        body must startWith("[{") and endWith("}]")
    }

    def countEntries = get("/count") {
        body must contain("{\"value\":3}")
    }

}
