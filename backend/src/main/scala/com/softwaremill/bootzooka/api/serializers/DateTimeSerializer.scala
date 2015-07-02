package com.softwaremill.bootzooka.api.serializers

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.ext.DateParser

class DateTimeSerializer extends CustomSerializer[DateTime](format => ({
  case JString(s) => new DateTime(DateParser.parse(s, format))
  case JNull => null
},
  {
    case d: DateTime => JString(ISODateTimeFormat.basicDateTime().print(d))
  }))