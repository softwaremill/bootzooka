package uitest

import org.scalatest.{FunSuite, BeforeAndAfter}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import pl.softwaremill.bootstrap.service.schedulers.DummyEmailSendingService

class BootstrapUITest extends FunSuite with EmbeddedJetty with BeforeAndAfter {
  var emailSendingService:DummyEmailSendingService = _
  before {
    System.setProperty("withInMemory", "true")
    startJetty()
    emailSendingService = context.getAttribute("EMAILSERVICE").asInstanceOf[DummyEmailSendingService]
    assert(emailSendingService != null)
  }

  after {
    System.clearProperty("withInMemory")
    stopJetty()
  }
}

trait EmbeddedJetty {
  protected var jetty: Server = null
  protected var context: ServletContext = null

  def startJetty() {
    jetty = new Server(8080)
    jetty setHandler prepareContext
    jetty.start()
  }

  private def prepareContext() = {
    val context = new WebAppContext()
    context setContextPath "/"
    context setResourceBase "bootstrap-ui/src/main/webapp"
    this.context = context.getServletContext
    context
  }


  def stopJetty() {
    jetty.stop()
  }
}
