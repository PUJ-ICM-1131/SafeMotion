package com.example.safemotion.data.model

data class AppUser(
    val uid: String,
    val name: String,
    val email: String
)

// Clave para comparar teléfonos sin importar espacios, guiones o el indicativo +57.
fun phoneKey(phone: String): String = phone.filter(Char::isDigit).takeLast(10)
