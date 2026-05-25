package com.foddy.app.domain.repository

import com.foddy.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun registerUser(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun updateProfile(name: String, email: String): Result<Unit>
    suspend fun logout()
}
