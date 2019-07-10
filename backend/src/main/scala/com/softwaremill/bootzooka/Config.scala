package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.email.EmailConfig
import com.softwaremill.bootzooka.infrastructure.{DBConfig, HttpConfig}
import com.softwaremill.bootzooka.passwordreset.PasswordResetConfig

case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, bootzooka: BootzookaConfig)
