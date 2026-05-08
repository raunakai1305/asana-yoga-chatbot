package com.asana.appium.pages;

import io.appium.java_client.android.AndroidDriver;

/**
 * Page Object for the Chat screen.
 *
 * Accessibility IDs come from ChatScreen.kt semantics blocks:
 *   "Message input field"  — OutlinedTextField
 *   "Send message"         — FilledIconButton (visible only when input is non-blank)
 *   "Hold to speak"        — Mic button (visible only when input is blank)
 */
public class ChatPage extends BasePage {

    // Accessibility IDs
    public static final String INPUT_FIELD  = "Message input field";
    public static final String SEND_BUTTON  = "Send message";
    public static final String MIC_BUTTON   = "Hold to speak";

    // Expected text content
    static final String WELCOME_SNIPPET     = "Namaste";
    static final String APP_BAR_TITLE       = "Asana";
    static final String TYPING_INDICATOR    = "● ● ●";

    public ChatPage(AndroidDriver driver, int timeoutSeconds) {
        super(driver, timeoutSeconds);
    }

    // ── Presence checks ──────────────────────────────────────────────────────

    public boolean isInputFieldVisible() {
        return isVisible(INPUT_FIELD);
    }

    public boolean isSendButtonVisible() {
        return isVisible(SEND_BUTTON, 2);
    }

    public boolean isMicButtonVisible() {
        return isVisible(MIC_BUTTON, 2);
    }

    public boolean isWelcomeMessageVisible() {
        return isTextVisible(WELCOME_SNIPPET);
    }

    public boolean isTypingIndicatorVisible() {
        return isTextVisible(TYPING_INDICATOR);
    }

    public boolean isAppBarTitleVisible() {
        return isTextVisible(APP_BAR_TITLE);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    public ChatPage typeMessage(String text) {
        type(INPUT_FIELD, text);
        return this;
    }

    public ChatPage tapSend() {
        tap(SEND_BUTTON);
        return this;
    }

    /** Sends via IME "Done/Send" action on the keyboard. */
    public ChatPage sendWithKeyboard() {
        waitForClickable(INPUT_FIELD).submit();
        return this;
    }

    public ChatPage sendMessage(String text) {
        typeMessage(text);
        hideKeyboard();
        tapSend();
        return this;
    }

    public VoicePage pressMic() {
        tap(MIC_BUTTON);
        return new VoicePage(driver, 10);
    }

    // ── Waiting helpers ───────────────────────────────────────────────────────

    /** Waits until a typing indicator disappears and a new bot message arrives. */
    public ChatPage waitForBotReply(int timeoutSeconds) {
        // Wait up to timeoutSeconds for the typing indicator to go away
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (!isTypingIndicatorVisible()) break;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return this;
    }

    /** Returns the text of the last message bubble visible on screen. */
    public String getLastMessageText() {
        // Bot messages come from the AI; use UiAutomator to fetch the last TextView
        // in the lazy column — this finds text in the last visible chat bubble
        return driver.findElements(
                io.appium.java_client.AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.TextView\")"
                )
        ).stream()
         .filter(el -> !el.getText().isBlank())
         .reduce((a, b) -> b)   // last element
         .map(el -> el.getText())
         .orElse("");
    }

    public int getMessageCount() {
        return driver.findElements(
                io.appium.java_client.AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.TextView\")"
                )
        ).stream()
         .filter(el -> !el.getText().isBlank())
         .toList()
         .size();
    }
}
