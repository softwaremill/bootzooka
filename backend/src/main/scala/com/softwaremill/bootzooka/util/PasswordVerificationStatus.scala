package com.softwaremill.bootzooka.util

trait PasswordVerificationStatus
case object Verified           extends PasswordVerificationStatus
case object VerificationFailed extends PasswordVerificationStatus
