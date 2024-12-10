package com.example.huna_app

data class User(
    var id: String = "",       // Значення за замовчуванням для id
    var name: String = "",     // Значення за замовчуванням для name
    var age: String = "",      // Значення за замовчуванням для age
    var address: String = ""   // Значення за замовчуванням для address
) {
    // Конструктор без параметрів для Firestore
    constructor() : this("", "", "", "")
}
