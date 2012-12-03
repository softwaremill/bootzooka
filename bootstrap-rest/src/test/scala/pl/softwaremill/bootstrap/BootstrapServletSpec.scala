package pl.softwaremill.bootstrap

import org.scalatra.test.specs2.ScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.matcher.ThrownExpectations
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Formats}

trait BootstrapServletSpec extends ScalatraSpec with Mockito with ThrownExpectations {

  val defaultJsonHeaders = Map("Content-Type" -> "application/json;charset=UTF-8")

  protected implicit val jsonFormats: Formats = DefaultFormats

  def mapToJson[T <% JValue](map: Map[String, T]): Array[Byte] = {
    compact(map2jvalue(map)).getBytes("UTF-8")
  }

  def stringToJson(string: String): JValue = {
    parse(string)
  }

  def mapToStringifiedJson[T <% JValue](map: Map[String, T]): String = {
    compact(map2jvalue(map))
  }

}
