package uitest.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import uitest.commands.SeleniumCommands;

public class RegistrationPage {

    private final WebDriver driver;
    SeleniumCommands sc;

    @FindBy(css = "#login")
    private WebElement loginField;

    @FindBy(css = "#email")
    private WebElement emailField;

    @FindBy(css = "#password")
    private WebElement passwordField;

    @FindBy(css = "#repeatPassword")
    private WebElement repeatPassField;

    @FindBy(css = "button[type=submit]")
    private WebElement registerButton;

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        sc = new SeleniumCommands(driver);
    }

    public void register(String login, String email, String password) throws Exception {
        openRegistrationPage();

        loginField.sendKeys(login);
        emailField.sendKeys(email);
        passwordField.sendKeys(password);
        repeatPassField.sendKeys(password);
        registerButton.click();
        sc.waitForFinishLoading();
    }

    public void openRegistrationPage() throws Exception {
        driver.get("http://localhost:8080/#/register");
        sc.waitForFinishLoading();
    }
}
