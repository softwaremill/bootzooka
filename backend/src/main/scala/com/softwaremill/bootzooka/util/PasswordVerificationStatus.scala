package com.softwaremill.bootzooka.util

sealed trait PasswordVerificationStatus
case object Verified           extends PasswordVerificationStatus
case object VerificationFailed extends PasswordVerificationStatus
