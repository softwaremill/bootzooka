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
    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)
    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.logout()
  }

}
