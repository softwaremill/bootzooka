package com.softwaremill.bootzooka.user.rememberme

import java.util.UUID

import org.joda.time.DateTime

case class RememberMeToken(id: UUID, selector: String, tokenHash: String, userId: UUID, validTo: DateTime)