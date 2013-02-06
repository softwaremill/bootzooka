package pl.softwaremill.bootstrap.rest

import org.specs2.mock.Mockito
import java.io.Writer
import org.json4s.JsonAST.{JString, JObject, JField}
import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.Matchers
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class JsonServletSpec extends FlatSpec with ShouldMatchers with Mockito {
  "writeJson" should "use escaped data" in {
    // Given
    val servlet = new JsonServletWithMockedMapper()
    val json = new JObject(List(JField("data", JString("<script>hack</script>"))))
    val escapedJson = new JObject(List(JField("data", JString("&lt;script&gt;hack&lt;/script&gt;"))))

    // When
    servlet.writeJson(json, mock[Writer])

    // Then
    there was one(servlet.mockedMapper).writeValue(any[Writer], Matchers.eq(escapedJson))
    there was no(servlet.mockedMapper).writeValue(any[Writer], Matchers.eq(json))
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
    there was one(servlet.mockedMapper).writeValue(any[Writer], Matchers.eq(jsonWithString))
    there was no(servlet.mockedMapper).writeValue(any[Writer], Matchers.eq(json))
    there was no(servlet.mockedMapper).writeValue(any[Writer], Matchers.eq(escapedJson))
  }

  class JsonServletWithMockedMapper extends JsonServlet {
    val mockedMapper = mock[ObjectMapper]
    override def mapper = mockedMapper
  }
}
