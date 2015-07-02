package com.softwaremill.bootzooka.api.serializers

import java.util.UUID

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

class UuidSerializer extends CustomSerializer[UUID](format => ({
  case JString(s) => UUID.fromString(s)
  case JNull => null
},
  {
    case u: UUID => JString(u.toString)
  }))
