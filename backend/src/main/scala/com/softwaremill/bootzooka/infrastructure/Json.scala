package com.softwaremill.bootzooka.infrastructure

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import com.softwaremill.bootzooka.util.{Id, asId}
import com.softwaremill.tagging.*

/** Import the members of this object when doing JSON serialisation or deserialization. */
object Json:
  given taggedIdCodec[U]: JsonValueCodec[Id @@ U] = new JsonValueCodec[Id @@ U]:
    override def decodeValue(in: JsonReader, default: Id @@ U): Id @@ U = in.readString(default).asId[U]
    override def encodeValue(x: Id @@ U, out: JsonWriter): Unit = out.writeVal(x)
    override def nullValue: Id @@ U = null.asInstanceOf[Id @@ U]

  given taggedStringCodec[U]: JsonValueCodec[String @@ U] = new JsonValueCodec[String @@ U]:
    override def decodeValue(in: JsonReader, default: String @@ U): String @@ U = in.readString(default).taggedWith[U]
    override def encodeValue(x: String @@ U, out: JsonWriter): Unit = out.writeVal(x)
    override def nullValue: String @@ U = null.asInstanceOf[String @@ U]
