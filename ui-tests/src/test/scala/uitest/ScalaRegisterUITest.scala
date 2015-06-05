package uitest

import com.jayway.awaitility.scala.AwaitilitySupport
import com.softwaremill.bootzooka.common.Utils
import org.fest.assertions.Assertions
import org.openqa.selenium.support.PageFactory
import uitest.pages.RegistrationPage

import scala.concurrent.ExecutionContext.Implicits.global

class ScalaRegisterUITest extends BootzookaUITest with AwaitilitySupport {
  final val LOGIN = Utils.randomString(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  final val EMAIL_SUBJECT = s"SoftwareMill Bootzooka - registration confirmation for user $LOGIN"

  test("register new user and send an email") {
    //given
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    //when
    emailService.run()

    //then
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")
    Assertions.assertThat(emailService.wasEmailSent(EMAIL, EMAIL_SUBJECT))
  }

  test("register - fail due to not matching passwords") {
    //given
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

    // when
    registrationPage.register(LOGIN, EMAIL, PASSWORD, Some(PASSWORD + "FooBarBaz"))
    emailService.run()

    //then
    Assertions.assertThat(messagesPage.isUMessageDisplayed("Passwords don't match!"))
    Assertions.assertThat(!emailService.wasEmailSent(EMAIL, EMAIL_SUBJECT))
  }


}
