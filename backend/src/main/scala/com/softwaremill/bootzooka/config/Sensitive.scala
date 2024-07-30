package com.softwaremill.bootzooka.config

import pureconfig.ConfigReader

case class Sensitive(value: String) extends AnyVal:
  override def toString: String = "***"

object Sensitive:
  given ConfigReader[Sensitive] = pureconfig.ConfigReader[String].map(Sensitive(_))
