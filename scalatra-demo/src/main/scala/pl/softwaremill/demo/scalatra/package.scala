package pl.softwaremill.demo.scalatra

trait JsonHelpers {

  import net.liftweb.json._
  import net.liftweb.json.ext._
  import net.liftweb.json.Extraction._

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  object Json {
    def apply(json: JValue, compacting: Boolean) = {
      val doc = render(json)
      if (compacting) compact(doc) else pretty(doc)
    }

    def apply(a: Any): Any = apply(decompose(a), compacting=true)
  }
}