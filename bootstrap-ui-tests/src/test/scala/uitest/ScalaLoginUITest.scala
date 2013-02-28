package uitest

import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import org.openqa.selenium.support.PageFactory
import pages.{MessagesPage, LoginPage}
import org.fest.assertions.Assertions

class ScalaLoginUITest extends BootstrapUITest {
  final val LOGIN = "regtest"
  final val PASSWORD = "test"

  ignore("login") {
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)

    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.logout()
  }

}
