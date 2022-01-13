package com.softwaremill.bootzooka.email.sender

import cats.effect.IO
import com.softwaremill.bootzooka.email.EmailData

trait EmailSender {
  def apply(email: EmailData): IO[Unit]
}
