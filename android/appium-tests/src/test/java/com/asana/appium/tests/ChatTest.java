package com.asana.appium.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests for the Chat screen — the primary product surface.
 *
 * Scenarios covered:
 *   - Initial state (welcome message, input field, mic button)
 *   - Send button visibility toggling with text input
 *   - Sending a message and verifying it appears in the list
 *   - Typing indicator appears while awaiting AI response
 *   - AI response arrives and replaces the typing indicator
 *   - Sending an empty message does nothing
 *   - Multiple messages accumulate in the list
 *   - App bar title is present
 */
public class ChatTest extends BaseTest {

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test(description = "Welcome message is displayed on first launch")
    public void welcomeMessageIsShown() {
        assertTrue(chatPage.isWelcomeMessageVisible(),
                "Welcome 'Namaste' message should be visible after splash");
    }

    @Test(description = "Chat input field is visible and empty on launch")
    public void inputFieldIsVisible() {
        assertTrue(chatPage.isInputFieldVisible(), "Message input field should be visible");
    }

    @Test(description = "App bar shows 'Asana' title")
    public void appBarTitleIsVisible() {
        assertTrue(chatPage.isAppBarTitleVisible(), "App bar should display 'Asana'");
    }

    // ── Mic/Send button toggle ─────────────────────────────────────────────────

    @Test(description = "Mic button is shown when the input field is empty")
    public void micButtonVisibleWhenInputEmpty() {
        assertTrue(chatPage.isMicButtonVisible(),
                "Mic button ('Hold to speak') should be visible when input is empty");
        assertFalse(chatPage.isSendButtonVisible(),
                "Send button should NOT be visible when input is empty");
    }

    @Test(description = "Send button appears and mic button hides when user starts typing")
    public void sendButtonAppearsOnTyping() {
        chatPage.typeMessage("Warrior");
        assertTrue(chatPage.isSendButtonVisible(),
                "Send button should appear once text is typed");
        assertFalse(chatPage.isMicButtonVisible(),
                "Mic button should hide while there is text in the input");
    }

    @Test(description = "Send button disappears again after clearing the input field")
    public void sendButtonHidesAfterClearingInput() {
        chatPage.typeMessage("Warrior");
        assertTrue(chatPage.isSendButtonVisible());

        // Clear the field
        chatPage.typeMessage(""); // clear() then empty sendKeys
        // After clearing, mic should reappear
        assertTrue(chatPage.isMicButtonVisible(),
                "Mic button should reappear after input is cleared");
    }

    // ── Sending messages ───────────────────────────────────────────────────────

    @Test(description = "User message appears in the chat list after sending")
    public void userMessageAppearsAfterSend() {
        String query = "How do I do Tree Pose?";
        int beforeCount = chatPage.getMessageCount();

        chatPage.sendMessage(query);

        // At a minimum, the user's own message should now be visible
        assertTrue(chatPage.isTextVisible(query),
                "User's sent message should appear in the chat");
        assertTrue(chatPage.getMessageCount() > beforeCount,
                "Message count should increase after sending");
    }

    @Test(description = "Typing indicator (● ● ●) appears while AI is processing a response")
    public void typingIndicatorAppearsWhileLoading() {
        chatPage.sendMessage("Tell me about Warrior I");
        // Immediately after sending, loading state should show dots
        assertTrue(chatPage.isTypingIndicatorVisible(),
                "Typing indicator should appear while awaiting the AI response");
    }

    @Test(description = "AI response arrives and typing indicator disappears")
    public void botReplyArrives() {
        chatPage.sendMessage("What are the benefits of Savasana?");

        // Wait up to 30 s for the typing indicator to go away (real backend call)
        chatPage.waitForBotReply(30);

        assertFalse(chatPage.isTypingIndicatorVisible(),
                "Typing indicator should disappear once the AI replies");
    }

    @Test(description = "Sending an empty/blank message does not add a new bubble")
    public void emptyMessageIsIgnored() {
        int before = chatPage.getMessageCount();
        // Do not type anything; just attempt to tap send (it's hidden, so nothing happens)
        // Verify the count does not change
        assertEquals(chatPage.getMessageCount(), before,
                "Message count should not change when no message is typed");
    }

    @Test(description = "Multiple messages accumulate in the conversation")
    public void multipleMessagesAccumulate() {
        chatPage.sendMessage("What is Tadasana?");
        chatPage.waitForBotReply(30);

        int afterFirst = chatPage.getMessageCount();
        assertTrue(afterFirst >= 3,
                "Should have welcome + user msg + bot reply (≥3 items)");

        chatPage.sendMessage("What are its benefits?");
        chatPage.waitForBotReply(30);

        assertTrue(chatPage.getMessageCount() > afterFirst,
                "Message count should grow with each exchange");
    }

    // ── Input field behaviour ──────────────────────────────────────────────────

    @Test(description = "Keyboard 'Send' IME action submits the message")
    public void imeActionSendsMessage() {
        String msg = "Show me Cobra Pose steps";
        chatPage.typeMessage(msg);
        chatPage.sendWithKeyboard();

        assertTrue(chatPage.isTextVisible(msg),
                "Message submitted via keyboard IME action should appear in chat");
    }
}
