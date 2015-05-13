package com.softwaremill.bootzooka.test

import scala.concurrent.{ExecutionContext, Future}

trait FutureTestSupport {

  implicit def ec: ExecutionContext

  def runFutures(futures: Future[_]*): Unit = {
    futures.reduce((f1, f2) => f1.flatMap(_ => f2))
  }

}
