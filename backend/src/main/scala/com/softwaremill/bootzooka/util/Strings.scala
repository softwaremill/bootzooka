package com.softwaremill.bootzooka.util

import java.util.Locale

object Strings:
  opaque type Id[T] = String
  opaque type LowerCased <: String = String
  opaque type Hashed <: String = String

  extension (s: String)
    def asId[T]: Id[T] = s
    def asHashed: Hashed = s
    def toLowerCased[T]: LowerCased = s.toLowerCase(Locale.ENGLISH)
end Strings
