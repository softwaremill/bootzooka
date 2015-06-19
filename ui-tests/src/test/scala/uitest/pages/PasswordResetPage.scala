package uitest.pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class PasswordResetPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  def url(code: String) = s"${sc.URL}password-reset?code=$code"

  @FindBy(name = "password")
  val passwordField: WebElement = null

  @FindBy(id = "repeatNotMatching")
  val repeatErrorText: WebElement = null

  @FindBy(name = "repeatPassword")
  val repeatPassField: WebElement = null

  @FindBy(css = "button[type=submit]")
  val resetButton: WebElement = null

  def resetPassword(password: String, repeatedPassword: String) {
    passwordField.sendKeys(password)
    repeatPassField.sendKeys(repeatedPassword)
    resetButton.click()
    sc.waitForFinishLoading()
  }

  def openPasswordResetPage(code: String) {
    driver.get(url(code))
    sc.waitForFinishLoading()
  }

  def getErrorText = repeatErrorText.getText
}