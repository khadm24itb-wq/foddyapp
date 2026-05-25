package com.foddy.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val phoneNumber: String = "",
    val address: String = "",
    val profilePictureUrl: String = "",
    val role: String = "USER"
)
