package uitest

class LoginUiSpec extends BaseUiSpec {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(RegUser, RegPass)

    messagesPage.isUserLogged(RegUser) should be(true)
    messagesPage.logout()
  }

}
