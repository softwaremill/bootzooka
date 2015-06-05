package uitest.pages

import org.openqa.selenium.{WebDriver, WebElement}
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class PasswordResetPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  def url(code: String) = s"${sc.URL}password-reset?code=$code"

  @FindBy(name = "password")
  val passwordField: WebElement = null

  @FindBy(name = "repeatPassword")
  val repeatPassField: WebElement = null

  @FindBy(css = "button[type=submit]")
  val loginButton: WebElement = null

  def resetPassword(password: String, repeatedPassword: String, afterClick: SeleniumCommands => Unit = _.waitForFinishLoading()) {
    passwordField.sendKeys(password)
    repeatPassField.sendKeys(repeatedPassword)
    loginButton.click()
    afterClick(sc)
  }

  def openPasswordResetPage(code: String) {
    driver.get(url(code))
    sc.waitForFinishLoading()
  }

}