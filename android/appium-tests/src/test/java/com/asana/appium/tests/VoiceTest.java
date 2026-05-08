package com.asana.appium.tests;

import com.asana.appium.pages.VoicePage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Tests for the Voice Recording screen.
 *
 * Note: actual speech-to-text accuracy cannot be tested in Appium automation.
 * These tests verify the UI state that surrounds the STT interaction:
 *   - Navigation to the voice screen when mic is pressed
 *   - Correct accessibility IDs and labels are present
 *   - Permission dialog is handled (auto-granted via capabilities)
 *   - Listening state labels are correct
 */
public class VoiceTest extends BaseTest {

    @Test(description = "Tapping 'Hold to speak' navigates to the voice recording screen")
    public void micButtonOpensVoiceScreen() {
        VoicePage voice = chatPage.pressMic();
        assertTrue(voice.isListeningMicVisible(),
                "Voice screen mic button ('Listening, release to send') should be visible");
    }

    @Test(description = "Voice screen shows 'Listening...' label while recording")
    public void voiceScreenShowsListeningLabel() {
        VoicePage voice = chatPage.pressMic();
        assertTrue(voice.isListeningLabelVisible(),
                "'Listening...' label should be shown on the voice screen");
    }

    @Test(description = "Voice screen shows 'Release to send' instruction")
    public void voiceScreenShowsReleaseHint() {
        VoicePage voice = chatPage.pressMic();
        assertTrue(voice.isReleaseToSendVisible(),
                "'Release to send' hint should be visible on the voice screen");
    }

    @Test(description = "Mic button on chat screen is only visible when the input field is empty")
    public void micButtonHiddenWhileTyping() {
        assertTrue(chatPage.isMicButtonVisible(),
                "Mic is shown when input is empty");
        chatPage.typeMessage("hello");
        assertTrue(chatPage.isSendButtonVisible(),
                "Send button appears once text is entered");
        assertTrue(!chatPage.isMicButtonVisible(),
                "Mic button is hidden while text is in the input field");
    }
}
