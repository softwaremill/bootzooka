package com.softwaremill.bootzooka.security

import java.time.Instant

import com.softwaremill.bootzooka.Id
import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
