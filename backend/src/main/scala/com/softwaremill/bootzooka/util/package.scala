package com.softwaremill.bootzooka.util

import java.util.Locale
import com.softwaremill.tagging.*
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint

type SecureRandomId <: String
type Id = SecureRandomId

extension (s: String)
  inline def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
  inline def lowerCased: String @@ LowerCased = s.toLowerCase(Locale.ENGLISH).taggedWith[LowerCased]
  inline def hashedPassword: String @@ PasswordHash = s.taggedWith[PasswordHash]

type ServerEndpoints = List[ServerEndpoint[Any, Identity]]
