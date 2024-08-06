package com.softwaremill.bootzooka.config

import pureconfig.ConfigReader

/** Wrapper class for any configuration strings which shouldn't be logged verbatim. */
case class Sensitive(value: String):
  override def toString: String = "***"

object Sensitive:
  given ConfigReader[Sensitive] = pureconfig.ConfigReader[String].map(Sensitive(_))
