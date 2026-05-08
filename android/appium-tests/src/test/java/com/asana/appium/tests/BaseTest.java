package com.asana.appium.tests;

import com.asana.appium.pages.ChatPage;
import com.asana.appium.pages.SplashPage;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    protected AndroidDriver driver;
    protected ChatPage chatPage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        // Every test starts on ChatPage — wait through the splash screen first
        SplashPage splash = new SplashPage(driver, DriverFactory.waitTimeout());
        chatPage = splash.waitForChatScreen(DriverFactory.splashDuration());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
