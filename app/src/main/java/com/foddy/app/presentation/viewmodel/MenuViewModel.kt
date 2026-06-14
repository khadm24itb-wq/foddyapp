package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Category
import com.foddy.app.domain.usecase.menu.AddMenuItemUseCase
import com.foddy.app.domain.usecase.menu.GetMenuItemsUseCase
import com.foddy.app.domain.usecase.menu.RemoveMenuItemUseCase
import com.foddy.app.domain.usecase.menu.UpdateMenuItemUseCase
import com.foddy.app.domain.usecase.menu.UploadFoodImageUseCase
import com.foddy.app.domain.usecase.menu.DeleteFoodImageUseCase
import com.foddy.app.core.Resource
import com.foddy.app.presentation.ui.state.UiState
import android.net.Uri
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
    private val updateMenuItemUseCase: UpdateMenuItemUseCase,
    private val removeMenuItemUseCase: RemoveMenuItemUseCase,
    private val uploadFoodImageUseCase: UploadFoodImageUseCase,
    private val deleteFoodImageUseCase: DeleteFoodImageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<FoodItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FoodItem>>> = _uiState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val uploadState: StateFlow<Resource<String>> = _uploadState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getMenuItemsUseCase.executeCategories().collect {
                _categories.value = it
            }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            uploadFoodImageUseCase(uri).collect { resource ->
                _uploadState.value = resource
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = Resource.Success("")
    }

    fun observeMenu(restaurantId: String? = null) {
        viewModelScope.launch {
            // Sử dụng invoke() của GetMenuItemsUseCase để lắng nghe Realtime
            getMenuItemsUseCase(restaurantId).collect { items ->
                _uiState.value = UiState.Success(items)
                _foodItems.value = items
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

    fun updateFoodItem(item: FoodItem, oldImageUrl: String? = null) {
        viewModelScope.launch {
            try {
                updateMenuItemUseCase(item)
                // If update successful and image changed, delete old image
                if (oldImageUrl != null && oldImageUrl != item.imageUrl && oldImageUrl.isNotEmpty()) {
                    deleteFoodImageUseCase(oldImageUrl)
                }
            } catch (e: Exception) {
                println("Error updating menu item: ${e.message}")
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
