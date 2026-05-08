package com.asana.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Splash screen — visible for ~2.5 s then auto-navigates to ChatPage.
 *
 * Compose does not assign contentDescription to the title/subtitle text on the
 * splash screen, so we fall back to UiAutomator text matching for text assertions
 * and rely on the timed navigation to arrive at ChatPage.
 */
public class SplashPage extends BasePage {

    // Text constants matching SplashScreen.kt
    static final String TITLE_TEXT    = "Asana";
    static final String SUBTITLE_TEXT = "Your Personal Yoga Guide";
    static final String TAGLINE_TEXT  = "Powered by Gemini AI";

    public SplashPage(AndroidDriver driver, int timeoutSeconds) {
        super(driver, timeoutSeconds);
    }

    public boolean isTitleVisible() {
        return isTextVisible(TITLE_TEXT);
    }

    public boolean isSubtitleVisible() {
        return isTextVisible(SUBTITLE_TEXT);
    }

    public boolean isTaglineVisible() {
        return isTextVisible(TAGLINE_TEXT);
    }

    /**
     * Waits for the splash screen to finish and the Chat screen to appear.
     * The splash auto-navigates after 2 500 ms; we wait up to splashWaitSeconds.
     */
    public ChatPage waitForChatScreen(int splashWaitSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(splashWaitSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.accessibilityId(ChatPage.INPUT_FIELD)));
        return new ChatPage(driver, splashWaitSeconds);
    }
}
