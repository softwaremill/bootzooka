package uitest.commands;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.util.concurrent.TimeUnit;

public class SeleniumCommands {
    private final WebDriver driver;
    public Wait<WebDriver> wait;

    public SeleniumCommands(WebDriver driver) {
        this.driver = driver;

        wait = new FluentWait<WebDriver>(driver)
                .withTimeout(20, TimeUnit.SECONDS.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS);
    }

    public void waitForFinishLoading() throws Exception {
        waitForElementInvisible(By.cssSelector("#ajaxthrobber"));
    }

    public void waitForElementClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForElementVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForElementInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForElementPresent(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private ExpectedCondition<Boolean> isElementNotVisible(final By locator) {
        return new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement toReturn = driver.findElement(locator);
                    return toReturn.isDisplayed();
                } catch (NoSuchElementException ex) {
                    return true;
                }
            }
        };
    }

    private ExpectedCondition<Boolean> isElementVisible(final By locator) {
        return new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement toReturn = driver.findElement(locator);
                    return toReturn.isDisplayed();
                } catch (NoSuchElementException ex) {
                    return false;
                }
            }
        };
    }

    public void waitForElement(final By locator) {
        wait.until(isElementVisible(locator));
    }
}
