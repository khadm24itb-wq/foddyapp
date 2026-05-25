package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    init {
        observeMenu()
    }

    private fun observeMenu() {
        viewModelScope.launch {
            menuRepository.getMenuItems().collect { items ->
                _foodItems.value = items
            }
        }
    }

    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                menuRepository.addMenuItem(item)
            } catch (e: Exception) {
                println("Error adding menu item: ${e.message}")
            }
        }
    }

    fun removeFoodItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                menuRepository.removeMenuItem(item)
            } catch (e: Exception) {
                println("Error removing menu item: ${e.message}")
            }
        }
    }
}
