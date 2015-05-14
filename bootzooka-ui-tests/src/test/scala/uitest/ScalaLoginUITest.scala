package uitest

import org.fest.assertions.Assertions

import scala.concurrent.ExecutionContext

class ScalaLoginUITest(implicit ec: ExecutionContext) extends BootzookaUITest {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.logout()
  }

}
