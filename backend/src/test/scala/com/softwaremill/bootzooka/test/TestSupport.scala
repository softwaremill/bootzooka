package com.softwaremill.bootzooka.test

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromString}
import com.softwaremill.bootzooka.http.Error_OUT

trait TestSupport:
  extension (v: Either[String, String])
    def shouldDeserializeTo[T: JsonValueCodec]: T = v.map(readFromString[T](_)).fold(s => throw new IllegalArgumentException(s), identity)
    def shouldDeserializeToError: String = readFromString[Error_OUT](v.fold(identity, s => throw new IllegalArgumentException(s))).error
