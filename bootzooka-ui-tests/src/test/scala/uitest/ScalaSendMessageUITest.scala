package uitest

import org.fest.assertions.Assertions
import com.softwaremill.bootzooka.common.Utils

import scala.concurrent.ExecutionContext

class ScalaSendMessageUITest(implicit ec: ExecutionContext) extends BootzookaUITest {
  final val MSG_TEXT = Utils.randomString(20)


  ignore("send message") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.sendMessage(MSG_TEXT)
    Assertions.assertThat(messagesPage.isUMessageDisplayed(MSG_TEXT)).isTrue()
    messagesPage.logout()
  }

}