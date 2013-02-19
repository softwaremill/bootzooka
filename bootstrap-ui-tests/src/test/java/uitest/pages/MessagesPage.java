package uitest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import uitest.commands.SeleniumCommands;

public class MessagesPage {
    private final WebDriver driver;
    SeleniumCommands sc;

    @FindBy(css = "#logoutLink")
    private WebElement logoutLink;

    @FindBy(css = ".alert-info")
    private WebElement alert;

    public MessagesPage(WebDriver driver) {
        this.driver = driver;
        sc = new SeleniumCommands(driver);
    }

    public void logout() throws Exception {
        logoutLink.click();
        SeleniumCommands sc = new SeleniumCommands(driver);
        sc.waitForFinishLoading();
    }

    public boolean isUserLogged() throws Exception {
        try {
            driver.findElement(By.cssSelector("#logoutLink"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getInfoText() {
       sc.waitForElementVisible(alert);
       return alert.getText();
    }
}
