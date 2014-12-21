package uitest

import uitest.pages.{EntriesByAuthorsPage}
import org.openqa.selenium.support.PageFactory
import org.fest.assertions.Assertions._
import org.openqa.selenium.By

class ScalaEntriesByAuthorsUITest extends BootzookaUITest {

  ignore("entries by authors without logged user") {
    val entriesByAuthorsPage: EntriesByAuthorsPage = PageFactory.initElements(driver, classOf[EntriesByAuthorsPage])

    entriesByAuthorsPage.openWithoutAuthor()

    assertThat(driver.getCurrentUrl).startsWith(loginPage.url)
  }

  ignore("entries by author with no selected author") {
    val entriesByAuthorsPage: EntriesByAuthorsPage = PageFactory.initElements(driver, classOf[EntriesByAuthorsPage])

    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    entriesByAuthorsPage.openWithoutAuthor()

    assertThat(entriesByAuthorsPage.areTwoRegisteredUsersDisplayed()).isTrue()
    assertThat(entriesByAuthorsPage.isSelectAnAuthorMessageDisplayed).isTrue()
  }

  ignore("entries by author with selected author with entries") {
    val entriesByAuthorsPage: EntriesByAuthorsPage = PageFactory.initElements(driver, classOf[EntriesByAuthorsPage])

    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    val authorWithEntries = beans.userService.findByLogin(REGUSER).get
    entriesByAuthorsPage.openWithAuthor(authorWithEntries)

    assertThat(entriesByAuthorsPage.areTwoRegisteredUsersDisplayed()).isTrue()
    assertThat(entriesByAuthorsPage.isEntriesByAuthorMessageDisplayed(authorWithEntries)).isTrue()
    assertThat(entriesByAuthorsPage.entries.findElements(By.cssSelector("p.entryText")).size()).isEqualTo(1)
    assertThat(entriesByAuthorsPage.entries.findElements(By.cssSelector("p.entryText")).get(0).getText).isEqualTo(MESSAGE)
  }

  ignore("entries by author with selected author with no entries") {
    val entriesByAuthorsPage: EntriesByAuthorsPage = PageFactory.initElements(driver, classOf[EntriesByAuthorsPage])

    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    val authorWithNoEntries = beans.userService.findByLogin("1" + REGUSER)
    entriesByAuthorsPage.openWithAuthor(authorWithNoEntries.get)

    assertThat(entriesByAuthorsPage.areTwoRegisteredUsersDisplayed()).isTrue()
    assertThat(entriesByAuthorsPage.isNoEntriesMessageDisplayed).isTrue()
    assertThat(entriesByAuthorsPage.entries.findElements(By.cssSelector("p.entryText")).size()).isEqualTo(0)
  }
}
