package com.asana.appium.pages;

import io.appium.java_client.android.AndroidDriver;

/**
 * Page Object for VoiceRecordingScreen.
 *
 * Accessibility ID from VoiceScreen.kt semantics block:
 *   "Listening, release to send"  — the animated mic button
 */
public class VoicePage extends BasePage {

    public static final String MIC_LISTENING  = "Listening, release to send";
    static final String LISTENING_TEXT        = "Listening...";
    static final String RELEASE_TO_SEND       = "Release to send";

    public VoicePage(AndroidDriver driver, int timeoutSeconds) {
        super(driver, timeoutSeconds);
    }

    public boolean isListeningMicVisible() {
        return isVisible(MIC_LISTENING, 5);
    }

    public boolean isListeningLabelVisible() {
        return isTextVisible(LISTENING_TEXT);
    }

    public boolean isReleaseToSendVisible() {
        return isTextVisible(RELEASE_TO_SEND);
    }
}
