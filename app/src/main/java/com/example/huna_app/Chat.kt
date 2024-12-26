package com.example.huna_app

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val timestamp: Long = 0L,
    val productId: String? = null
)


