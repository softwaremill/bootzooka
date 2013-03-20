package uitest

import org.fest.assertions.Assertions
import com.softwaremill.common.util.RichString

class ScalaSendMessageUITest extends BootzookaUITest {
  final val MSG_TEXT = RichString.generateRandom(20)


  test("send message") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.sendMessage(MSG_TEXT)
    Assertions.assertThat(messagesPage.isUMessageDisplayed(MSG_TEXT)).isTrue()
    messagesPage.logout()
  }

}