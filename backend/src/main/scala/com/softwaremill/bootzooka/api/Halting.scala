package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.common.StringJsonWrapper
import org.scalatra._

trait Halting {
  self: Control =>

  private def haltWithActionResultAndMessage(f: Any => ActionResult, message: String) = halt(f(StringJsonWrapper(message)))

  def haltWithBadRequest(message: String) {
    haltWithActionResultAndMessage(BadRequest.apply(_), message)
  }

  def haltWithUnauthorized(message: String) {
    haltWithActionResultAndMessage(Unauthorized.apply(_), message)
  }

  def haltWithForbidden(message: String) {
    haltWithActionResultAndMessage(Forbidden.apply(_), message)
  }

  def haltWithForbiddenIf(f: Boolean) {
    if (f) haltWithForbidden("Action forbidden")
  }

  def haltWithNotFound(message: String) {
    haltWithActionResultAndMessage(NotFound.apply(_), message)
  }

  def haltWithConflict(message: String) {
    haltWithActionResultAndMessage(Conflict.apply(_), message)
  }
}
