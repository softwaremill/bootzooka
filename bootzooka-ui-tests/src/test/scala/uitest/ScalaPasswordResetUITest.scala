package uitest

import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.fest.assertions.Assertions
import org.openqa.selenium.By

import scala.util.Success

class ScalaPasswordResetUITest extends BootzookaUITest {

  private val validCode = "SOME00CODE"
  private val invalidCode = validCode + "666"

  override def beforeAll(): Unit = {
    super.beforeAll()
    registerUserIfNotExists("someUser", "some-user@example.com", "somePass") match {
      case Success(_) =>
        beans.userDao.findByLoginOrEmail("someUser").foreach { user =>
          val passResetCode = PasswordResetCode(validCode, user.id)
          beans.codeDao.store(passResetCode)
        }
      case _ =>
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    beans.codeDao.load(validCode).foreach(beans.codeDao.delete)
    removeUsers("someUser")
  }

  test("password-reset should reset password") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getInfoText.contains("Your password has been changed"))
  }

  test("password-reset should not reset password due to missing code") {
    passwordRestPage.openPasswordResetPage("")
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getErrorText.contains("Wrong or malformed password recovery code."))
  }

  test("password-reset should not reset password due to invalid code") {
    passwordRestPage.openPasswordResetPage(invalidCode)
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getErrorText.contains("Wrong or malformed password recovery code."))
  }

  test("password-reset should do nothing if password & its repetition differ") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "notMatching", sc => {
      sc.waitForElementVisible(By.cssSelector(".password-repeat-error"))
      Assertions.assertThat(true)
    })
  }


}
