package uitest

import com.softwaremill.bootzooka.version.BuildInfo._
import org.fest.assertions.Assertions

import scala.concurrent.ExecutionContext.Implicits.global

class MainPageUITest extends BootzookaUITest {

  test("application version") {
    // when
    mainPage.open()

    // then
    Assertions.assertThat(mainPage.getVersionString).isEqualTo(s"Build $buildSha, $buildDate")
  }
}
