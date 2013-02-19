package uitest.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import uitest.commands.SeleniumCommands;


public class LoginPage {

    private final WebDriver driver;
    SeleniumCommands sc;

    @FindBy(css = "#login")
    private WebElement loginField;

    @FindBy(css = "#password")
    private WebElement passwordField;

    @FindBy(css = "button[type=submit]")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        sc = new SeleniumCommands(driver);
    }

    public void login(String login, String password) throws Exception {
        loginField.sendKeys(login);
        passwordField.sendKeys(password);
        loginButton.click();
        sc.waitForFinishLoading();
    }

    public void openLoginPage() throws Exception {
        driver.get("http://localhost:8080/#/login");
        sc.waitForFinishLoading();
    }
}
