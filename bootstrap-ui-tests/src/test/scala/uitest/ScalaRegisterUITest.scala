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
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])

    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")

    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)
    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    Assertions.assertThat(emailSendingService.isEmailScheduled(EMAIL)).isFalse()
  }
}
