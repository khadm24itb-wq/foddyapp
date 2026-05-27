package com.foddy.app.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "USER", // USER, DRIVER, RESTAURANT, ADMIN
    val phone: String = "",
    val address: String = "",
    val avatar: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
)
