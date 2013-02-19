package uitest

import org.scalatest.{FunSuite, BeforeAndAfter}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

class BootstrapUITest extends FunSuite with EmbeddedJetty with BeforeAndAfter {
  before {
    startJetty()
  }

  after {
    stopJetty()
  }
}

trait EmbeddedJetty {
  private var jetty: Server = _

  def startJetty() {
    jetty = new Server(8080)
    jetty setHandler prepareContext
    jetty.start()
  }

  private def prepareContext() = {
    val context = new WebAppContext()
    context setContextPath "/"
    context setResourceBase "bootstrap-ui/src/main/webapp"
    context
  }


  def stopJetty() {
    jetty.stop()
  }
}
