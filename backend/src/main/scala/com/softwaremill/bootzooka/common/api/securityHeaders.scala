package com.softwaremill.bootzooka.common.api

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

// https://www.owasp.org/index.php/List_of_useful_HTTP_headers
// https://www.owasp.org/index.php/Clickjacking_Defense_Cheat_Sheet
private[api] sealed trait SecurityHeaderDirective[H] {
  def value: String
}

private[api] sealed trait RequestSecurityHeaderDirective[H] extends SecurityHeaderDirective[H]

private[api] sealed trait ResponseSecurityHeaderDirective[H] extends SecurityHeaderDirective[H]

object `X-Frame-Options` extends ModeledCustomHeaderCompanion[`X-Frame-Options`] {
  override val name = "X-Frame-Options"

  override def parse(value: String) = Try(new `X-Frame-Options`(value))

  def apply(value: ResponseSecurityHeaderDirective[`X-Frame-Options`]) = new `X-Frame-Options`(value.value)

  case object `DENY` extends ResponseSecurityHeaderDirective[`X-Frame-Options`] {
    override def value: String = "DENY"
  }

  case object `SAMEORIGIN` extends ResponseSecurityHeaderDirective[`X-Frame-Options`] {
    override def value: String = "SAMEORIGIN"
  }

  final case class `ALLOW-FROM`(uri: String) extends ResponseSecurityHeaderDirective[`X-Frame-Options`] {
    override def value: String = s"ALLOW-FROM $uri"
  }
}

final case class `X-Frame-Options`(value: String) extends ModeledCustomHeader[`X-Frame-Options`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `X-Frame-Options`
}

object `X-Content-Type-Options` extends ModeledCustomHeaderCompanion[`X-Content-Type-Options`] {
  override val name = "X-Content-Type-Options"

  override def parse(value: String) = Try(new `X-Content-Type-Options`(value))

  def apply(value: ResponseSecurityHeaderDirective[`X-Content-Type-Options`]) = new `X-Content-Type-Options`(value.value)

  case object `nosniff` extends ResponseSecurityHeaderDirective[`X-Content-Type-Options`] {
    override def value: String = "nosniff"
  }
}

final case class `X-Content-Type-Options`(value: String) extends ModeledCustomHeader[`X-Content-Type-Options`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `X-Content-Type-Options`
}

object `X-XSS-Protection` extends ModeledCustomHeaderCompanion[`X-XSS-Protection`] {
  override val name = "X-XSS-Protection"

  override def parse(value: String) = Try(new `X-XSS-Protection`(value))

  def apply(value: ResponseSecurityHeaderDirective[`X-XSS-Protection`]) = new `X-XSS-Protection`(value.value)

  case object `0` extends ResponseSecurityHeaderDirective[`X-XSS-Protection`] {
    override def value: String = "0"
  }

  case object `1; mode=block` extends ResponseSecurityHeaderDirective[`X-XSS-Protection`] {
    override def value: String = "1; mode=block"
  }

  final case class `1; report=`(uri: String) extends ResponseSecurityHeaderDirective[`X-XSS-Protection`] {
    override def value: String = s"1; report=$uri"
  }
}

final case class `X-XSS-Protection`(value: String) extends ModeledCustomHeader[`X-XSS-Protection`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `X-XSS-Protection`
}

