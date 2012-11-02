package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.domain.Entry
import org.scalatra.util.NotEmpty

class RestServlet extends ScalatraServlet with ScalateSupport with JsonHelpers {

  before() {
    contentType = "application/json;charset=UTF-8"
  }

  get("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.load(id.getOrElse(-1)) match {
          case entry: Some[Entry] =>
            Json(entry)
          case None =>
            null
        }
      case _ => null
    }
  }

  get("/") {
    Json(EntryService.loadAll)
  }

  post("/") {
    (params.get("author"), params.get("text")) match {
      case (NotEmpty(username), NotEmpty(password)) =>
        EntryService.add(new Entry(0, params("author"), params("text")))
      case _ =>
        null
    }
  }

  delete("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.remove(id.getOrElse(-1))
      case _ => null
    }
  }

}

object SafeInt {
  val IntPattern = "-?([0-9]+)"

  def apply(o: Option[String]): Option[Int] = apply(o.getOrElse(""))

  def apply(o: String): Option[Int] = if (o.matches(IntPattern)) Some(o.toInt) else None

  def unapply(o: String): Option[Int] = if (o.matches(IntPattern)) Some(o.toInt) else None

  def orNone(i: Any): Option[Int] = i match {
    case i: Int => Some(i)
    case l: Long => Some(l.toInt)
    case _ => None
  }
}