package com.foddy.app.domain.model

data class User(
    val id: String = "",
    val email: String,
    val name: String,
    val phoneNumber: String = "",
    val address: String = "",
    val profilePictureUrl: String = "",
    val role: String = "USER",
    val isLoggedIn: Boolean = false
)
