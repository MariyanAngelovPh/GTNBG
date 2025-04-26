package com.example.gettoknowbulgaria.data

data class Landmark(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val manicipality: String = "",
    val city: String = "",
    val type: String = "",
    val image_path: String = "",
    val location: String = ""
) {
    // Празен конструктор за Firebase
    constructor() : this(0, "", "", "", "", "", "", "")
}
