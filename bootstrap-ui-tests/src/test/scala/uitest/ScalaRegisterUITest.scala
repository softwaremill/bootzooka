package uitest

import java.util.concurrent.TimeUnit
import pages.{LoginPage, MessagesPage, RegistrationPage}
import org.openqa.selenium.support.PageFactory
import org.fest.assertions.Assertions
import pl.softwaremill.common.util.RichString
import com.jayway.awaitility.scala.AwaitilitySupport
import com.jayway.awaitility.Awaitility._

class ScalaRegisterUITest extends BootstrapUITest with AwaitilitySupport {
  final val LOGIN = RichString.generateRandom(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  test("register") {
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")

    await atMost(60, TimeUnit.SECONDS) until {
      emailSendingService.wasEmailSent(EMAIL, "SML Bootstrap - registration confirmation for user " + LOGIN)
    }
  }

  test("login") {
    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.logout()

    driver.quit()
  }
}
