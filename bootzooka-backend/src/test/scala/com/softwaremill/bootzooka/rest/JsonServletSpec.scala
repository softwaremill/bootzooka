package com.softwaremill.bootzooka.rest

import java.io.Writer
import org.json4s.JsonAST.{JString, JObject, JField}
import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import org.mockito.Matchers._

class JsonServletSpec extends FlatSpec with scalatest.Matchers with MockitoSugar {
  "writeJson" should "use escaped data" in {
    // Given
    val servlet = new JsonServletWithMockedMapper()
    val json = new JObject(List(JField("data", JString("<script>hack</script>"))))
    val escapedJson = new JObject(List(JField("data", JString("&lt;script&gt;hack&lt;/script&gt;"))))

    // When
    servlet.writeJson(json, mock[Writer])

    // Then
    verify(servlet.mockedMapper) writeValue(any[Writer], Matchers.eq(escapedJson))
    verify(servlet.mockedMapper, never()) writeValue(any[Writer], Matchers.eq(json))
  }

  "writeJson" should "use unwrapped and not escaped data" in {
    // Given
    val servlet = new JsonServletWithMockedMapper()
    val jsonWithString = JString("<script>hack</script>")
    val json = new JObject(List(JField("notEscapedData", jsonWithString)))
    val escapedJson = new JObject(List(JField("notEscapedData", JString("&lt;script&gt;hack&lt;/script&gt;"))))

    // When
    servlet.writeJson(json, mock[Writer])

    // Then
    verify(servlet.mockedMapper) writeValue(any[Writer], Matchers.eq(jsonWithString))
    verify(servlet.mockedMapper, never()) writeValue(any[Writer], Matchers.eq(json))
    verify(servlet.mockedMapper, never()) writeValue(any[Writer], Matchers.eq(escapedJson))
  }

  class JsonServletWithMockedMapper extends JsonServlet {
    val mockedMapper = mock[ObjectMapper]
    override def mapper = mockedMapper
  }
}
