package pl.softwaremill.demo.spray

import spray.json.DefaultJsonProtocol

case class MyLog(id: Int, author: String, text: String, date: String)

object Entries {

  var list: List[MyLog] = List(
    MyLog(1, "Jan Kowalski", "A taki sobie tweet", "2012/09/01"),
    MyLog(2, "Tadeusz Nowak", "Bardzo wazny tweet", "2012/09/12")
  )

}

object MyLogJsonProtocol extends DefaultJsonProtocol {
  implicit val myLogFormat = jsonFormat4(MyLog)
}

