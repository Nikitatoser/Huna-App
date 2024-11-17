package com.example.huna_app

data class User(
    var id: String,
    var name: String,
    var surname: String,
    var age: Int,
    var address: String,
    var imageUrl: String,
)
{
    constructor() : this("", "", "", 0, "", "")

}