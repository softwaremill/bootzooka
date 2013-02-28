package uitest

import org.openqa.selenium.support.PageFactory
import pages.{MessagesPage, LoginPage}
import org.fest.assertions.Assertions
import pl.softwaremill.common.util.RichString

class ScalaSendMessageUITest extends BootstrapUITest {
  final val LOGIN = "regtest"
  final val PASSWORD = "test"
  final val MSG_TEXT = RichString.generateRandom(20)


  test("send message") {
    val loginPage: LoginPage = PageFactory.initElements(driver, classOf[LoginPage])
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)

    val messagesPage: MessagesPage = PageFactory.initElements(driver, classOf[MessagesPage])
    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.sendMessage(MSG_TEXT);
    Assertions.assertThat(messagesPage.isUMessageDisplayed(MSG_TEXT)).isTrue()
    messagesPage.logout()

    driver.quit()
  }

}