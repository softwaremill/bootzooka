package com.softwaremill.bootzooka.test

import scala.io.Source

object Shell {
  def runShellCommand(command: String): Process = {
    Runtime.getRuntime.exec(Array("sh", "-c", command))
  }

  def readProcessPids(processGrepStrings: String*): Iterator[String] = {
    val cmd = new StringBuilder("ps ax")
    for (processGrepString <- processGrepStrings) {
      cmd.append(" | grep ").append(processGrepString)
    }
    cmd.append(" | grep -v grep | cut -c1-5")

    val getPidProcess = runShellCommand(cmd.toString())
    Source.fromInputStream(getPidProcess.getInputStream).getLines()
  }
}
