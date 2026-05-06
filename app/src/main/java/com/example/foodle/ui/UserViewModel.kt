package com.example.foodle.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodle.model.AppDatabase
import com.example.foodle.model.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false
)

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("foodle_prefs", Context.MODE_PRIVATE)
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    private val _user = MutableStateFlow(
        UserProfile(
            name = sharedPrefs.getString("user_name", "") ?: "",
            email = sharedPrefs.getString("user_email", "") ?: "",
            isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        )
    )
    val user: StateFlow<UserProfile> = _user

    // ĐĂNG KÝ VỚI ROOM
    fun register(name: String, email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    Toast.makeText(getApplication(), "Email đã tồn tại!", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                } else {
                    userDao.registerUser(UserEntity(email, name, password))
                    loginLocal(name, email)
                    onComplete(true)
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Lỗi Room: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    // ĐĂNG NHẬP VỚI ROOM
    fun loginCloud(email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userEntity = userDao.getUserByEmail(email)
                if (userEntity != null) {
                    if (userEntity.password == password) {
                        loginLocal(userEntity.name, userEntity.email)
                        onComplete(true)
                    } else {
                        Toast.makeText(getApplication(), "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                } else {
                    Toast.makeText(getApplication(), "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    fun updateProfile(newName: String, email: String) {
        viewModelScope.launch {
            userDao.updateName(email, newName)
            loginLocal(newName, email)
        }
    }

    private fun loginLocal(name: String, email: String) {
        sharedPrefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
        _user.value = UserProfile(name, email, true)
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
        _user.value = UserProfile()
    }
}
