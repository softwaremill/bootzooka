package uitest

import java.util.concurrent.TimeUnit

import com.softwaremill.bootzooka.service.email.DummyEmailService
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

class BaseUiSpec extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter with ScalaFutures {
  val RegUser = "reguser"
  val RegPass = "regpass"
  val RegMail = "reguser@regmail.pl"

  var driver: FirefoxDriver = _
  var emailService: DummyEmailService = _
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
    beans = context.getAttribute("appObject").asInstanceOf[Beans]
    registerUserIfNotExists(RegUser, RegMail, RegPass)
    registerUserIfNotExists("1" + RegUser, "1" + RegMail, RegPass)
    emailService = beans.emailService.asInstanceOf[DummyEmailService]
  }

  /**
   * Register a new user if doesn't exist.
   *
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
    removeUsers(RegUser, "1" + RegUser)
    stopJetty()
  }
}