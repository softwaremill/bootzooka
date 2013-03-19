package pl.softwaremill.bootstrap.service.schedulers

import com.typesafe.scalalogging.slf4j.Logging

trait EmailSendingService extends Runnable with Logging with EmailScheduler
