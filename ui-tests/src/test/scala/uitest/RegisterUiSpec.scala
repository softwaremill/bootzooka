package uitest

import com.jayway.awaitility.scala.AwaitilitySupport
import com.softwaremill.bootzooka.common.Utils
import org.fest.assertions.Assertions._
import org.scalatest.BeforeAndAfterEach
import uitest.pages.RegistrationPage

class RegisterUiSpec extends BaseUiSpec with AwaitilitySupport with BeforeAndAfterEach {

  val Login = Utils.randomString(5)
  val Email = Login + "@example.org"
  val Password = "test"

  final val EmailSubject = s"SoftwareMill Bootzooka - registration confirmation for user $Login"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    emailService.reset()
  }

  test("register new user and send an email") {
    //given
    val registrationPage = createPage(classOf[RegistrationPage])

    //when
    registrationPage.register(Login, Email, Password)

    //then
    assertThat(messagesPage.getInfoText) contains "User registered successfully"
    assertThat(emailService.wasEmailSent(Email, EmailSubject))
  }

  test("register - fail due to not matching passwords") {
    //given
    val registrationPage = createPage(classOf[RegistrationPage])

    // when
    registrationPage.register(Login, Email, Password, Some(Password + "FooBarBaz"))

    //then
    assertThat(registrationPage.getPassErrorText) contains "Passwords don't match!"
    assertThat(emailService.wasEmailSent(Email, EmailSubject)).isFalse()
  }

}
