package uitest

import org.fest.assertions.Assertions

class ScalaLoginUITest extends BootstrapUITest {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.logout()
  }

}
