package com.example.huna_app

data class Product(
    var id: String = "",  // Змінюємо тип на String
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var address: String = "",
    var deliveryType: String = "",
    var addDate: String = "",
    var available: Boolean = true,
    var category: String = "",
    var imageUrl: String = "",
    var ownerId: String = ""
) {
    constructor() : this(
        id = "",
        name = "",
        description = "",
        price = 0.0,
        address = "",
        deliveryType = "",
        addDate = "",
        available = true,
        category = "",
        imageUrl = "",
        ownerId = ""
    )
}

