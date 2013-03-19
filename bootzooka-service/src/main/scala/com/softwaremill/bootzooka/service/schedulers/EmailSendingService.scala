package com.softwaremill.bootzooka.service.schedulers

import com.typesafe.scalalogging.slf4j.Logging

trait EmailSendingService extends Runnable with Logging with EmailScheduler
