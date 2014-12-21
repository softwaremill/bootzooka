package uitest

import com.jayway.awaitility.scala.AwaitilitySupport
import com.softwaremill.bootzooka.common.Utils
import org.fest.assertions.Assertions
import org.openqa.selenium.support.PageFactory
import uitest.pages.RegistrationPage

class ScalaRegisterUITest extends BootzookaUITest with AwaitilitySupport {
  final val LOGIN = Utils.randomString(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  test("register") {
    //given
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    //when
    emailService.run()

    //then
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")
    emailService.wasEmailSent(EMAIL, "SoftwareMill Bootzooka - registration confirmation for user " + LOGIN)
  }
}
