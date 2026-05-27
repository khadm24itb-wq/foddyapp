package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.usecase.menu.AddMenuItemUseCase
import com.foddy.app.domain.usecase.menu.GetMenuItemsUseCase
import com.foddy.app.domain.usecase.menu.RemoveMenuItemUseCase
import com.foddy.app.domain.util.Resource
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val addMenuItemUseCase: AddMenuItemUseCase,
    private val removeMenuItemUseCase: RemoveMenuItemUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<FoodItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FoodItem>>> = _uiState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    fun observeMenu(restaurantId: String? = null) {
        viewModelScope.launch {
            getMenuItemsUseCase.executeWithCache(restaurantId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (resource.data.isNullOrEmpty()) {
                            _uiState.value = UiState.Loading
                        } else {
                            _uiState.value = UiState.Success(resource.data)
                        }
                    }
                    is Resource.Success -> {
                        _uiState.value = UiState.Success(resource.data ?: emptyList())
                        _foodItems.value = resource.data ?: emptyList()
                    }
                    is Resource.Error -> {
                        if (resource.data.isNullOrEmpty()) {
                            _uiState.value = UiState.Error(resource.message ?: "Unknown error")
                        } else {
                            _uiState.value = UiState.Success(resource.data)
                        }
                    }
                }
            }
        }
    }

    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                addMenuItemUseCase(item)
            } catch (e: Exception) {
                println("Error adding menu item: ${e.message}")
            }
        }
    }

    fun removeFoodItem(item: FoodItem) {
        viewModelScope.launch {
            try {
                removeMenuItemUseCase(item)
            } catch (e: Exception) {
                println("Error removing menu item: ${e.message}")
            }
        }
    }
}
