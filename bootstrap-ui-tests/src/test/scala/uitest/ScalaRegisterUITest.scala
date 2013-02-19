package uitest

import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import pages.{LoginPage, MessagesPage, RegistrationPage}
import org.openqa.selenium.support.PageFactory
import org.fest.assertions.Assertions
import pl.softwaremill.common.util.RichString

class ScalaRegisterUITest extends BootstrapUITest {
  final val LOGIN = RichString.generateRandom(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  test("register") {
    val driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])
    registrationPage.register(LOGIN, EMAIL, PASSWORD)

    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")

    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)
    Assertions.assertThat(messagesPage.isUserLogged).isTrue()
  }
}
