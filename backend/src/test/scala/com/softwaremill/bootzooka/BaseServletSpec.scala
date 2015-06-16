package com.softwaremill.bootzooka

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.test.scalatest.ScalatraFlatSpec
import org.scalatest.mock.MockitoSugar
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}

trait BaseServletSpec extends ScalatraFlatSpec with MockitoSugar {

  implicit val httpRequest = mock[HttpServletRequest]
  implicit val httpResponse = mock[HttpServletResponse]

  val defaultJsonHeaders = Map("Content-Type" -> "application/json;charset=UTF-8")

  protected implicit val jsonFormats: Formats = DefaultFormats

  // Since scalatra 2.3 with jetty 9 throws an exception when single path is mapped to many servlets,
  // we have to perform additional servlets & mappings clean up here
  override def addServlet(servlet: HttpServlet, path: String): Unit = {

    def removeExistingServletAndMappings(): Unit = {
      val servletHandler = servletContextHandler.getServletHandler
      servletHandler.setServletMappings(servletHandler.getServletMappings.filterNot(_.getPathSpecs.contains(path)))
      servletHandler.setServlets(servletHandler.getServlets.filterNot(_.getServlet.getClass == servlet.getClass))
    }

    removeExistingServletAndMappings()
    super.addServlet(servlet, path)
  }

  def mapToJson[T <% JValue](map: Map[String, T]): Array[Byte] = {
    mapToStringifiedJson(map).getBytes("UTF-8")
  }

  def stringToJson(string: String): JValue = {
    parse(string)
  }

  def mapToStringifiedJson[T <% JValue](map: Map[String, T]): String = {
    compact(render(map2jvalue(map)))
  }
}
