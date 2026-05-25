package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState(isLoading = true))
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // Compatibility property for existing UI screens
    val user: StateFlow<User> = _uiState
        .map { it.user ?: User("", "", false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User("", "", false))

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser().collect { user ->
                    _uiState.value = UserUiState(
                        user = user,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun register(name: String, email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            userRepository.registerUser(name, email, password)
                .onSuccess {
                    onComplete(true)
                }
                .onFailure {
                    onComplete(false)
                }
        }
    }

    fun loginCloud(email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            userRepository.login(email, password)
                .onSuccess {
                    onComplete(true)
                }
                .onFailure {
                    onComplete(false)
                }
        }
    }

    fun signInWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(user = it, isLoading = false)
                    onComplete(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
                    onComplete(false)
                }
        }
    }

    fun updateProfile(newName: String, email: String) {
        viewModelScope.launch {
            userRepository.updateProfile(newName, email)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
