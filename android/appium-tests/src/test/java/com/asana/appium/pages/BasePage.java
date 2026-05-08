package com.asana.appium.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final AndroidDriver driver;
    protected final WebDriverWait wait;

    public BasePage(AndroidDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    protected WebElement waitForVisible(String accessibilityId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.accessibilityId(accessibilityId)
        ));
    }

    protected WebElement waitForClickable(String accessibilityId) {
        return wait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.accessibilityId(accessibilityId)
        ));
    }

    protected boolean isVisible(String accessibilityId) {
        try {
            return driver.findElement(AppiumBy.accessibilityId(accessibilityId)).isDisplayed();
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    protected boolean isVisible(String accessibilityId, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.accessibilityId(accessibilityId)));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void tap(String accessibilityId) {
        waitForClickable(accessibilityId).click();
    }

    protected void type(String accessibilityId, String text) {
        WebElement el = waitForClickable(accessibilityId);
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(String accessibilityId) {
        return waitForVisible(accessibilityId).getText();
    }

    protected void hideKeyboard() {
        try {
            driver.hideKeyboard();
        } catch (Exception ignored) {
            // keyboard not present
        }
    }

    // UiAutomator text search — used when accessibility ID is unavailable
    protected WebElement findByText(String text) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + text.replace("\"", "\\\"") + "\")"
                )
        ));
    }

    protected boolean isTextVisible(String text) {
        try {
            return findByText(text).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
