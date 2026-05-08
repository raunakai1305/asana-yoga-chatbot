package com.asana.app

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val imageUrl: String? = null,
    val isImageLoading: Boolean = false
)
