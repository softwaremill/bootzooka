package uitest

import org.fest.assertions.Assertions

class ScalaLoginUITest extends BootstrapUITest {
  final val LOGIN = "regtest"
  final val PASSWORD = "test"


  test("login") {
    loginPage.openLoginPage()
    loginPage.login(LOGIN, PASSWORD)

    Assertions.assertThat(messagesPage.isUserLogged(LOGIN)).isTrue()
    messagesPage.logout()
  }

}
