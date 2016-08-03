package uitest

import com.softwaremill.bootzooka.common.Utils
import org.scalatest.BeforeAndAfterEach
import uitest.pages.RegistrationPage

class RegisterUiSpec extends BaseUiSpec with BeforeAndAfterEach {

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
    messagesPage.getInfoText should include ("User registered successfully")
    emailService.wasEmailSent(Email, EmailSubject) should be (true)
  }

  test("register - fail due to not matching passwords") {
    //given
    val registrationPage = createPage(classOf[RegistrationPage])

    // when
    registrationPage.register(Login, Email, Password, Some(Password + "FooBarBaz"))

    //then
    registrationPage.getPassErrorText should include ("Passwords don't match!")
    emailService.wasEmailSent(Email, EmailSubject) should be (false)
  }

}
