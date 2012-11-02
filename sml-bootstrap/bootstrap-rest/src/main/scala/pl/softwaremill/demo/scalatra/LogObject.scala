package pl.softwaremill.demo.scalatra

case class LogObject(id: Int, author: String, text: String)

object Entries {

  var list = List(
    LogObject(1, "Jan Kowalski", "Short message"),
    LogObject(2, "Piotr Nowak", "Very long message")
  )

}
