package com.asana.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Namaste! 🙏 I'm Asana, your personal yoga pose guide. Ask me about any yoga pose and I'll walk you through it step by step.", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        _messages.value = _messages.value + ChatMessage(text, isUser = true)
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.sendMessage(
                    ChatRequest(session_id = sessionId, message = text)
                )
                _messages.value = _messages.value + ChatMessage(response.reply, isUser = false)
            } catch (e: Exception) {
                _error.value = "Couldn't connect to Asana. Please try again."
                _messages.value = _messages.value + ChatMessage(
                    "I'm having trouble connecting right now. Please check your connection and try again. 🙏",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
