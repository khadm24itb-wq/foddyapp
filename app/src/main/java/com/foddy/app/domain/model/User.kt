package com.foddy.app.domain.model

data class User(
    val email: String,
    val name: String,
    val phoneNumber: String = "",
    val address: String = "",
    val profilePictureUrl: String = "",
    val role: String = "USER", // USER, DRIVER, RESTAURANT_OWNER
    val isLoggedIn: Boolean = false
)
