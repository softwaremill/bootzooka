package com.softwaremill.bootzooka.test

import org.scalatest.BeforeAndAfterEach

trait ClearSQLDataAfterEach extends BeforeAndAfterEach {
  this: FlatSpecWithSQL =>

  override protected def afterEach() {
    try {
      clearData()
    } catch {
      case e: Exception => e.printStackTrace()
    }

    super.afterEach()
  }
}
