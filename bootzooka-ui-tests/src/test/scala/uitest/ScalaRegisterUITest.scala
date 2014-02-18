package uitest

import java.util.concurrent.TimeUnit
import pages.RegistrationPage
import org.openqa.selenium.support.PageFactory
import org.fest.assertions.Assertions
import com.jayway.awaitility.scala.AwaitilitySupport
import com.jayway.awaitility.Awaitility._
import com.softwaremill.bootzooka.common.Utils

class ScalaRegisterUITest extends BootzookaUITest with AwaitilitySupport {
  final val LOGIN = Utils.randomString(5)
  final val EMAIL = LOGIN + "@example.org"
  final val PASSWORD = "test"

  test("register") {
    val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

    registrationPage.register(LOGIN, EMAIL, PASSWORD)
    Assertions.assertThat(messagesPage.getInfoText).contains("User registered successfully")

    await atMost(60, TimeUnit.SECONDS) until {
      emailService.wasEmailSent(EMAIL, "SoftwareMill Bootzooka - registration confirmation for user " + LOGIN)
    }
  }
}
