package com.example.huna_app

data class Favorite(
    val userId: String = "",  // Значення за замовчуванням
    val productLinks: List<String> = emptyList()  // Значення за замовчуванням
) {
    // Потрібен конструктор без параметрів для Firestore
    constructor() : this("", emptyList())
}


