package com.asana.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val PREFS_NAME = "asana_prefs"
private const val KEY_SESSION_ID = "server_session_id"

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var serverSessionId: String? = prefs.getString(KEY_SESSION_ID, null)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        if (serverSessionId != null) {
            loadHistory()
        } else {
            _messages.value = listOf(
                ChatMessage(
                    "Namaste! 🙏 I'm Asana, your personal yoga pose guide. Ask me about any yoga pose and I'll walk you through it step by step.",
                    isUser = false
                )
            )
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = RetrofitClient.api.getHistory(serverSessionId!!)
                if (history.messages.isEmpty()) {
                    _messages.value = listOf(
                        ChatMessage(
                            "Namaste! 🙏 I'm Asana, your personal yoga pose guide. Ask me about any yoga pose and I'll walk you through it step by step.",
                            isUser = false
                        )
                    )
                } else {
                    _messages.value = history.messages.map { msg ->
                        ChatMessage(content = msg.content, isUser = msg.role == "user")
                    }
                }
            } catch (e: Exception) {
                Log.d("Raunak", "History load failed: ${e.message}")
                _messages.value = listOf(
                    ChatMessage(
                        "Namaste! 🙏 I'm Asana, your personal yoga pose guide. Ask me about any yoga pose and I'll walk you through it step by step.",
                        isUser = false
                    )
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        _messages.value = _messages.value + ChatMessage(text, isUser = true)
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Step 1: get text response
                val response = RetrofitClient.api.sendMessage(
                    ChatRequest(session_id = serverSessionId, message = text)
                )
                serverSessionId = response.session_id
                prefs.edit().putString(KEY_SESSION_ID, response.session_id).apply()

                // Add bot message immediately with image shimmer active
                _messages.value = _messages.value + ChatMessage(
                    content = response.reply,
                    isUser = false,
                    imageUrl = null,
                    isImageLoading = true
                )
                _isLoading.value = false
                Log.d("Raunak", "Text received, fetching image in parallel")

                // Step 2: fetch image in background, update message when ready
                launch {
                    try {
                        val imageResp = RetrofitClient.api.getImage(ImageRequest(message = text))
                        Log.d("Raunak", "Image received: ${imageResp.image_url}")
                        val msgs = _messages.value.toMutableList()
                        val idx = msgs.indexOfLast { !it.isUser }
                        if (idx >= 0) {
                            msgs[idx] = msgs[idx].copy(
                                imageUrl = imageResp.image_url,
                                isImageLoading = false
                            )
                            _messages.value = msgs
                        }
                    } catch (e: Exception) {
                        Log.d("Raunak", "Image fetch failed: ${e.message}")
                        val msgs = _messages.value.toMutableList()
                        val idx = msgs.indexOfLast { !it.isUser }
                        if (idx >= 0) {
                            msgs[idx] = msgs[idx].copy(isImageLoading = false)
                            _messages.value = msgs
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("Raunak", "Send failed: ${e.message}")
                _error.value = "Couldn't connect to Asana. Please try again."
                _messages.value = _messages.value + ChatMessage(
                    "I'm having trouble connecting right now. Please check your connection and try again. 🙏",
                    isUser = false
                )
                _isLoading.value = false
            }
        }
    }
}
