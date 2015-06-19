package uitest

import org.fest.assertions.Assertions
import com.softwaremill.bootzooka.common.Utils

class SendMessageUiSpec extends BaseUiSpec {
  final val MSG_TEXT = Utils.randomString(20)


  test("send message") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.sendMessage(MSG_TEXT)
    Assertions.assertThat(messagesPage.isUMessageDisplayed(MSG_TEXT)).isTrue()
    messagesPage.logout()
  }

}