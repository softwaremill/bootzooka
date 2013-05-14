package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import com.softwaremill.bootzooka.Beans
import com.softwaremill.bootzooka.service.schedulers.DummyEmailSendingService
import pages.{MessagesPage, LoginPage}
import org.openqa.selenium.support.PageFactory

class BootzookaUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter {
  final val REGUSER = "reguser"
  final val REGPASS = "regpass"
  final val REGMAIL = "reguser@regmail.pl"

  final val MESSAGE = "Dummy message"

  var driver: FirefoxDriver = _
  var emailService: DummyEmailSendingService = _
  var loginPage: LoginPage = _
  var messagesPage: MessagesPage = _
  var beans: Beans = _

  override def beforeAll() {
    sys.props.put("withInMemory", "true")
    startJetty()
    beans = context.getAttribute("bootzooka").asInstanceOf[Beans]
    beans.userService.registerNewUser(REGUSER, REGMAIL, REGPASS)
    beans.userService.registerNewUser("1" + REGUSER, "1" + REGMAIL, REGPASS)
    emailService = beans.emailSendingService.asInstanceOf[DummyEmailSendingService]
    beans.entryService.add(REGUSER, MESSAGE)
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
    context setResourceBase "bootzooka-ui/src/main/webapp"
    this.context = context.getServletContext
    context
  }


  def stopJetty() {
    jetty.stop()
  }
}