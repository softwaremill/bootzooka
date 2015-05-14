package uitest

import com.softwaremill.bootzooka.version.BootzookaBuildInfo._
import org.fest.assertions.Assertions

import scala.concurrent.ExecutionContext

class MainPageUITest(implicit ec: ExecutionContext) extends BootzookaUITest {

  test("application version") {
    // when
    mainPage.open()

    // then
    Assertions.assertThat(mainPage.getVersionString).isEqualTo(s"Build $buildSha, $buildDate")
  }
}
