package uitest

import java.util.concurrent.TimeUnit
import pages.RegistrationPage
import org.openqa.selenium.support.PageFactory
import org.fest.assertions.Assertions
import pl.softwaremill.common.util.RichString
import com.jayway.awaitility.scala.AwaitilitySupport
import com.jayway.awaitility.Awaitility._
import pages.{MessagesPage, LoginPage}

class ScalaRegisterUITest extends BootstrapUITest with AwaitilitySupport {
  final val LOGIN = RichString.generateRandom(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  test("register") {
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")

    await atMost(60, TimeUnit.SECONDS) until {
      emailSendingService.wasEmailSent(EMAIL, "SML Bootstrap - registration confirmation for user " + LOGIN)
    }
  }

  test("login") {
    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])
    loginPage.openLoginPage()
    loginPage.login(EMAIL, PASSWORD)
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.logout()
  }
}
