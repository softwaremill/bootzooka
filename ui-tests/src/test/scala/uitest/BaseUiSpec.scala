package uitest

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.Http.ServerBinding
import com.softwaremill.bootzooka.email.application.DummyEmailService
import com.softwaremill.bootzooka.passwordreset.application.SqlPasswordResetCodeSchema
import com.softwaremill.bootzooka.user.application.SqlUserSchema
import com.softwaremill.bootzooka.{DependencyWiring, Main}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.PageFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, Matchers}
import uitest.pages.{LoginPage, MainPage, MessagesPage, PasswordResetPage}

import scala.util.Try

class BaseUiSpec extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfter with ScalaFutures {
  val RegUser = "reguser"
  val RegPass = "regpass"
  val RegMail = "reguser@regmail.pl"

  var driver: FirefoxDriver = _
  var emailService: DummyEmailService = _
  var loginPage: LoginPage = _
  var messagesPage: MessagesPage = _
  var passwordRestPage: PasswordResetPage = _
  var mainPage: MainPage = _
  var businessLogic: DependencyWiring = _
  var binding: ServerBinding = _

  override def beforeAll() {
    val (startFuture, _beans) = new Main().start()
    businessLogic = _beans

    binding = startFuture.futureValue

    registerUserIfNotExists(RegUser, RegMail, RegPass)
    registerUserIfNotExists("1" + RegUser, "1" + RegMail, RegPass)
    emailService = businessLogic.emailService.asInstanceOf[DummyEmailService]
  }

  /**
   * Register a new user if doesn't exist.
   *
   * @return boolean value (wrapped within scala.util.Try) indicating
   *         if new user was created or an existing user was found
   */
  protected def registerUserIfNotExists(login: String, email: String, pass: String): Try[Boolean] = Try {
    if (businessLogic.userDao.findByLowerCasedLogin(login).futureValue.isEmpty) {
      businessLogic.userService.registerNewUser(login, email, pass).futureValue
      true
    }
    else {
      false
    }
  }

  lazy val schema = new SqlPasswordResetCodeSchema with SqlUserSchema {
    override protected val database = businessLogic.sqlDatabase

    import database.driver.api._

    val allSchemas = passwordResetCodes.schema ++ users.schema

    def drop() = database.db.run(allSchemas.drop)

    def create() = database.db.run(allSchemas.create)
  }

  before {
    schema.create()
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    loginPage = createPage(classOf[LoginPage])
    messagesPage = createPage(classOf[MessagesPage])
    passwordRestPage = createPage(classOf[PasswordResetPage])
    mainPage = createPage(classOf[MainPage])
  }

  after {
    schema.drop()
    driver.close()
    driver = null
  }

  override def afterAll() {
    binding.unbind().futureValue
    businessLogic.system.terminate()
  }

  def createPage[T](clazz: Class[T]): T = PageFactory.initElements(driver, clazz)
}
