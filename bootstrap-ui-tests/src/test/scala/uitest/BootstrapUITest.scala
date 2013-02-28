package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import pl.softwaremill.bootstrap.service.schedulers.DummyEmailSendingService
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import pages.{MessagesPage, LoginPage}
import org.openqa.selenium.support.PageFactory

class BootstrapUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter {
  var emailSendingService: DummyEmailSendingService = _
  var driver: FirefoxDriver = _
  var loginPage: LoginPage = _
  var messagesPage: MessagesPage = _

  override def beforeAll() {
    System.setProperty("withInMemory", "true")
    startJetty()
    emailSendingService = context.getAttribute("EMAILSERVICE").asInstanceOf[DummyEmailSendingService]
    assert(emailSendingService != null)
  }

  before {
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    loginPage = PageFactory.initElements(driver, classOf[LoginPage])
    messagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
  }

  after {
    driver.close
    driver = null
  }

  override def afterAll() {
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