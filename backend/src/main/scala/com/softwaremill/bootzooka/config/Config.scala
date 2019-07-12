package com.softwaremill.bootzooka.config

import com.softwaremill.bootzooka.email.EmailConfig
import com.softwaremill.bootzooka.http.HttpConfig
import com.softwaremill.bootzooka.infrastructure.DBConfig
import com.softwaremill.bootzooka.passwordreset.PasswordResetConfig
import com.softwaremill.bootzooka.user.UserConfig

/**
  * Maps to the `application.conf` file. Configuration for all modules of the application.
  */
case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, user: UserConfig)
