package uitest.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class LoginPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  val url = sc.URL + "login"

  @FindBy(name = "login")
  val loginField: WebElement = null

  @FindBy(name = "password")
  val passwordField: WebElement = null

  @FindBy(css = "button[type=submit]")
  val loginButton: WebElement = null

  def login(login: String, password: String) {
    loginField.sendKeys(login)
    passwordField.sendKeys(password)
    loginButton.click()
    sc.waitForFinishLoading()
  }

  def openLoginPage() {
    driver.get(url)
    sc.waitForFinishLoading()
  }
}