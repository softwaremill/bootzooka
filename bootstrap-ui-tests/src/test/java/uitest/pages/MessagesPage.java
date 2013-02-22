package uitest.pages;

import org.openqa.selenium.By;
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

    @FindBy(css = "textarea")
    private WebElement messageField;

    @FindBy(css = "input[type=submit]")
    private WebElement sendButton;

    public MessagesPage(WebDriver driver) {
        this.driver = driver;
        sc = new SeleniumCommands(driver);
    }

    public void logout() throws Exception {
        logoutLink.click();
        sc.waitForFinishLoading();
    }

    public void sendMessage(String messagetext) throws Exception {
        messageField.sendKeys(messagetext);
        sendButton.click();
        sc.waitForFinishLoading();
    }

    public boolean isUserLogged(String user) throws Exception {
        sc.waitForElementVisible(By.linkText("Logged in as " + user));
        return true;
    }

    public boolean isUMessageDisplayed(String message) throws Exception {
        sc.waitForElementVisible(By.xpath("//p[text()='" + message + "']"));
        return true;
    }

    public String getInfoText() {
       sc.waitForElementVisible(By.cssSelector("#info-message"));
       return alert.getText();
    }
}
