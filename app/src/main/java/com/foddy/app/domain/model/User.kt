package com.foddy.app.domain.model

data class User(
    val email: String,
    val name: String,
    val isLoggedIn: Boolean = false
)
