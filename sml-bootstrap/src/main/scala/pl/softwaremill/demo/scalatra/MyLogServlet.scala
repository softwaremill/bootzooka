package pl.softwaremill.demo.scalatra

import org.scalatra._
import scalate.ScalateSupport


class MyLogServlet extends ScalatraServlet with ScalateSupport {

  override def defaultTemplateFormat = "mustache"

  before() {
    contentType = "text/html;charset=UTF-8"
  }

  get("/") {
    mustache("index", "logs" -> Entries.list)
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }

}
