package com.example.huna_app

data class Favorite(
    val userId: String = "",
    val productLinks: List<String> = emptyList()
) {

    constructor() : this("", emptyList())
}


