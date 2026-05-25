package com.foddy.app.presentation.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.foddy.app.domain.model.CartItem
import com.foddy.app.domain.model.FoodItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> = _cartItems

    fun addToCart(foodItem: FoodItem) {
        val existingItem = _cartItems.find { it.foodItem.id == foodItem.id }
        if (existingItem != null) {
            val index = _cartItems.indexOf(existingItem)
            _cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            _cartItems.add(CartItem(foodItem, 1))
        }
    }

    fun removeFromCart(foodItem: FoodItem) {
        val existingItem = _cartItems.find { it.foodItem.id == foodItem.id }
        if (existingItem != null) {
            val index = _cartItems.indexOf(existingItem)
            if (existingItem.quantity > 1) {
                _cartItems[index] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                _cartItems.removeAt(index)
            }
        }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    val totalPrice: Double
        get() = _cartItems.sumOf { it.foodItem.price * it.quantity }
}
