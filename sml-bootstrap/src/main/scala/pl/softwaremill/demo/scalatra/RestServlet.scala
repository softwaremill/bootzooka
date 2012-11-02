package pl.softwaremill.demo.scalatra

import org.scalatra._
import scalate.ScalateSupport
import util.NotEmpty

class RestServlet extends ScalatraServlet with ScalateSupport with JsonHelpers {

  before() {
    contentType = "application/json;charset=UTF-8"
  }

  get("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        Entries.list.find(_.id == id.getOrElse(-1)) match {
          case log: Some[LogObject] =>
            Json(log)
          case None =>
            null
        }
      case _ => null
    }
  }

  get("/") {
    Json(Entries.list)
  }

  post("/") {
    (params.get("author"), params.get("text")) match {
      case (NotEmpty(username), NotEmpty(password)) =>
        Entries.list = LogObject(Entries.list.length + 1, params("author"), params("text")) :: Entries.list
      case _ =>
        null
    }
  }

  delete("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        Entries.list.find(_.id == id.getOrElse(-1)) match {
          case log: Some[LogObject] =>
            Entries.list = Entries.list - log.getOrElse(null)
          case None =>
            null
        }
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
