package uitest.pages

import org.openqa.selenium.support.FindBy
import org.openqa.selenium.{WebElement, WebDriver}
import uitest.commands.SeleniumCommands

class MainPage(driver: WebDriver) {

  private val sc: SeleniumCommands = new SeleniumCommands(driver)

  @FindBy(css = ".footer span.pull-right")
  val versionContainer: WebElement = null

  def getVersionString = versionContainer.getText

  def open() {
    driver.get(sc.URL)
    sc.waitForFinishLoading()
  }
}
