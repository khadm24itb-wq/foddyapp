package com.example.foodle.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.foodle.model.CartItem
import com.example.foodle.model.FoodItem

class CartViewModel : ViewModel() {
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
