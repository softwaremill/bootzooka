package com.softwaremill.bootzooka

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import java.net.InetSocketAddress

trait EmbeddedJetty {
  protected var jetty: Server = null

  def startJetty() {
    jetty = new Server(jettyAddress)
    jetty.setHandler(prepareContext())
    jetty.start()
  }

  protected def prepareContext() = {
    val context = new WebAppContext()
    context.setContextPath("/")
    setResourceBase(context)
    context
  }

  def stopJetty() {
    jetty.stop()
  }

  protected def setResourceBase(context: WebAppContext)

  def embeddedJettyConfig: EmbeddedJettyConfig

  lazy val jettyAddress = new InetSocketAddress(embeddedJettyConfig.webServerHost, embeddedJettyConfig.webServerPort)
}
