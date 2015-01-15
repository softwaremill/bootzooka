package uitest.commands

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Wait

import java.util.concurrent.TimeUnit

class SeleniumCommands(driver: WebDriver) {
  final val URL = "http://localhost:8080/#/"

  var fluentwait: Wait[WebDriver] = new FluentWait[WebDriver](driver)
    .withTimeout(20, TimeUnit.SECONDS)
    .pollingEvery(100, TimeUnit.MILLISECONDS)

  def waitForFinishLoading() {
    waitForElementInvisible(By.cssSelector("#loading-indicator"))
  }

  def waitForElementClickable(locator: By) {
    fluentwait.until(ExpectedConditions.elementToBeClickable(locator))
  }

  def waitForElementVisible(element: WebElement) {
    fluentwait.until(ExpectedConditions.visibilityOf(element))
  }

  def waitForElementVisible(locator: By) {
    fluentwait.until(ExpectedConditions.visibilityOfElementLocated(locator))
  }

  def waitForElementInvisible(locator: By) {
    fluentwait.until(ExpectedConditions.invisibilityOfElementLocated(locator))
  }

  def waitForElementPresent(locator: By) {
    fluentwait.until(ExpectedConditions.presenceOfElementLocated(locator))
  }
}
