package uitest.pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands
import com.softwaremill.bootzooka.service.data.UserJson

class EntriesByAuthorsPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  val url = sc.URL + "entries/author"

  @FindBy(css = "#entries")
  val entries: WebElement = null

  def areTwoRegisteredUsersDisplayed(): Boolean = {
    sc.waitForElementVisible(By.xpath("//ul[@id='authors']/li/a[text()='reguser']"))
    sc.waitForElementVisible(By.xpath("//ul[@id='authors']/li/a[text()='1reguser']"))
    true
  }

  def isSelectAnAuthorMessageDisplayed: Boolean = {
    sc.waitForElementVisible(By.xpath("//div[text()='Please select an author']"))
    true
  }

  def isNoEntriesMessageDisplayed: Boolean = {
    sc.waitForElementVisible(By.xpath("//div[text()='This user has not posted anything yet.']"))
    true
  }

  def isEntriesByAuthorMessageDisplayed(author: UserJson): Boolean = {
    sc.waitForElementVisible(By.xpath(s"//h2[text()='Entries by ${author.login}']"))
    true
  }

  def openWithoutAuthor() {
    driver.get(url)
    sc.waitForFinishLoading()
  }

  def openWithAuthor(author: UserJson) {
    driver.get(url + "/" + author.id)
    sc.waitForFinishLoading()
  }
}