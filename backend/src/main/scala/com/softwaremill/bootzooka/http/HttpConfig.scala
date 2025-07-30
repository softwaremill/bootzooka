package com.softwaremill.bootzooka.http

import pureconfig.ConfigReader

case class HttpConfig(host: String, port: Int) derives ConfigReader
