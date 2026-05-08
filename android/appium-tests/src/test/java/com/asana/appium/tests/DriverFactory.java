package com.asana.appium.tests;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

public class DriverFactory {

    private static final Properties props = loadProps();

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream is = DriverFactory.class.getClassLoader()
                .getResourceAsStream("android.properties")) {
            if (is != null) p.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load android.properties", e);
        }
        return p;
    }

    static AndroidDriver createDriver() {
        String appPath = Paths.get(
                System.getProperty("user.dir"), props.getProperty("app.path")
        ).normalize().toAbsolutePath().toString();

        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(props.getProperty("device.name"))
                .setPlatformVersion(props.getProperty("platform.version"))
                .setUdid(props.getProperty("device.udid"))
                .setApp(appPath)
                .setAppPackage(props.getProperty("app.package"))
                .setAppActivity(props.getProperty("app.activity"))
                .setAutomationName("UiAutomator2")
                .setNoReset(false)
                .setFullReset(false)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(300));

        // Speed up tests by disabling window animations
        options.setCapability("disableWindowAnimation", true);

        try {
            return new AndroidDriver(
                    new URL(props.getProperty("appium.server.url")), options
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AndroidDriver", e);
        }
    }

    static int waitTimeout() {
        return Integer.parseInt(props.getProperty("wait.timeout.seconds", "20"));
    }

    static int splashDuration() {
        return Integer.parseInt(props.getProperty("splash.duration.seconds", "4"));
    }
}
