package com.example.huna_app

data class User(
    var id: String = "",
    var name: String = "",
    var age: String = "",
    var address: String = ""
) {
    constructor() : this("", "", "", "")
}
