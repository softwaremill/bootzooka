package uitest

import java.util.concurrent.TimeUnit

import com.softwaremill.bootzooka.service.email.DummyEmailSendingService
import com.softwaremill.bootzooka.{Beans, EmbeddedJetty, EmbeddedJettyConfig}
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.webapp.WebAppContext
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.PageFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import uitest.pages.{MainPage, LoginPage, MessagesPage, PasswordResetPage}

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class BootzookaUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter with ScalaFutures {
  final val REGUSER = "reguser"
  final val REGPASS = "regpass"
  final val REGMAIL = "reguser@regmail.pl"

  final val MESSAGE = "Dummy message"

  var driver: FirefoxDriver = _
  var emailService: DummyEmailSendingService = _
  var loginPage: LoginPage = _
  var messagesPage: MessagesPage = _
  var passwordRestPage: PasswordResetPage = _
  var mainPage: MainPage = _
  var beans: Beans = _

  override protected def setResourceBase(context: WebAppContext): Unit = {
    val webappDirInsideJar = context.getClass.getClassLoader.getResource("webapp").toExternalForm
    context.setWar(webappDirInsideJar)
  }

  val embeddedJettyConfig = new EmbeddedJettyConfig {
    def rootConfig = ConfigFactory.load()
  }

  override def beforeAll() {
    startJetty()
    beans = context.getAttribute("bootzooka").asInstanceOf[Beans]
    registerUserIfNotExists(REGUSER, REGMAIL, REGPASS)
    registerUserIfNotExists("1" + REGUSER, "1" + REGMAIL, REGPASS)
    emailService = beans.emailScheduler.asInstanceOf[DummyEmailSendingService]
  }

  /**
   * Register a new user if doesn't exist.
   *
   * @param login
   * @param pass
   * @param email
   * @return boolean value (wrapped within scala.util.Try) indicating
   *         if new user was created or an existing user was found
   */
  protected def registerUserIfNotExists(login: String, email: String, pass: String): Try[Boolean] = Try {
    val userService = beans.userService
    if (userService.findByLogin(login).futureValue.isEmpty) {
      userService.registerNewUser(login, email, pass).futureValue
      true
    }
    false
  }

  before {
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    loginPage = PageFactory.initElements(driver, classOf[LoginPage])
    messagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    passwordRestPage = PageFactory.initElements(driver, classOf[PasswordResetPage])
    mainPage = PageFactory.initElements(driver, classOf[MainPage])
  }

  after {
    driver.close()
    driver = null
  }

  protected def removeUsers(logins: String*): Unit = {
    implicit val ec: ExecutionContext = global
    for {
      login <- logins
      user <- beans.userDao.findByLoginOrEmail("someUser").futureValue
    } {
      beans.userDao.remove(user.id).futureValue
    }
  }

  override def afterAll() {
    removeUsers(REGUSER, "1" + REGUSER)
    stopJetty()
  }
}