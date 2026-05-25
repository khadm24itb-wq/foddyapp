package com.foddy.app.data.repository

import android.content.Context
import com.foddy.app.data.local.UserDao
import com.foddy.app.data.model.UserEntity
import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : UserRepository {

    private val sharedPrefs = context.getSharedPreferences("foddy_prefs", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<User?>(
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            User(
                sharedPrefs.getString("user_email", "") ?: "",
                sharedPrefs.getString("user_name", "") ?: "",
                true
            )
        } else null
    )

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    override suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        return try {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("Email đã tồn tại!"))
            } else {
                userDao.registerUser(UserEntity(email, name, password))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByEmail(email)
            if (userEntity != null) {
                if (userEntity.password == password) {
                    val user = User(userEntity.email, userEntity.name, true)
                    loginLocal(user.name, user.email)
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("Mật khẩu không chính xác"))
                }
            } else {
                Result.failure(Exception("Tài khoản không tồn tại!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, email: String): Result<Unit> {
        return try {
            userDao.updateName(email, name)
            loginLocal(name, email)
            _currentUser.value = User(email, name, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sharedPrefs.edit().clear().apply()
        _currentUser.value = null
    }

    private fun loginLocal(name: String, email: String) {
        sharedPrefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}
