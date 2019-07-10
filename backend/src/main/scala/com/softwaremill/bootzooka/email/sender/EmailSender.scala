package com.softwaremill.bootzooka.email.sender

import com.softwaremill.bootzooka.email.EmailData
import monix.eval.Task

trait EmailSender {
  def apply(email: EmailData): Task[Unit]
}
