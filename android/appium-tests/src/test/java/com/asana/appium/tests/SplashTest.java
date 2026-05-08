package com.asana.appium.tests;

import com.asana.appium.pages.SplashPage;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Tests the SplashScreen which appears for ~2.5 s on first launch.
 * These tests do NOT extend BaseTest because BaseTest already waits past the splash.
 */
public class SplashTest {

    private AndroidDriver driver;
    private SplashPage splashPage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        // Immediately capture splash — do not wait for it to disappear
        splashPage = new SplashPage(driver, DriverFactory.waitTimeout());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test(description = "App title 'Asana' is shown on the splash screen")
    public void splashShowsTitle() {
        assertTrue(splashPage.isTitleVisible(), "Splash title 'Asana' should be visible");
    }

    @Test(description = "Subtitle is shown on the splash screen")
    public void splashShowsSubtitle() {
        assertTrue(splashPage.isSubtitleVisible(), "'Your Personal Yoga Guide' subtitle should be visible");
    }

    @Test(description = "Powered-by tagline is shown at the bottom of splash")
    public void splashShowsTagline() {
        assertTrue(splashPage.isTaglineVisible(), "'Powered by Gemini AI' tagline should be visible");
    }

    @Test(description = "Splash auto-navigates to chat screen after ~2.5 s")
    public void splashAutoNavigatesToChat() {
        // waitForChatScreen will fail with a timeout exception if chat never arrives
        var chat = splashPage.waitForChatScreen(DriverFactory.splashDuration());
        assertTrue(chat.isInputFieldVisible(), "Chat input field should be visible after splash");
    }
}
