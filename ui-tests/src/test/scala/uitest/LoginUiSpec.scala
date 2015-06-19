package uitest

import org.fest.assertions.Assertions

class LoginUiSpec extends BaseUiSpec {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(RegUser, RegPass)

    Assertions.assertThat(messagesPage.isUserLogged(RegUser)).isTrue()
    messagesPage.logout()
  }

}
