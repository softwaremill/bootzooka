package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import pl.softwaremill.bootstrap.Beans
import pl.softwaremill.bootstrap.service.schedulers.DummyEmailSendingService
import pages.{MessagesPage, LoginPage}
import org.openqa.selenium.support.PageFactory

class BootstrapUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter with Beans {
  var driver: FirefoxDriver = _
  var emailService: DummyEmailSendingService = _
  var loginPage: LoginPage = _
  var messagesPage: MessagesPage = _

  override def beforeAll() {
    sys.props.put("withInMemory", "true")
    startJetty()
    userService.registerNewUser("regtest", "regtest@test.pl", "test")
    emailService = emailSendingService.asInstanceOf[DummyEmailSendingService]
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
    sys.props.remove("withInMemory")
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