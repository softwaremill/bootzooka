package com.softwaremill.bootzooka.http

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class HttpConfig(host: String, port: Int) derives ConfigReader
