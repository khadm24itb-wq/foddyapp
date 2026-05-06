package com.example.foodle.model

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val imageRes: String = "",
    val rating: Double = 0.0,
    val calories: Int = 0,
    val isFlashSale: Boolean = false
)

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageRes: String = "",
    val rating: Double = 0.0,
    val deliveryTime: String = "",
    val deliveryFee: Double = 0.0,
    val menu: List<FoodItem> = emptyList(),
    val category: String = ""
)

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    var quantity: Int = 0
)

data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val avatar: String = "",
    val rating: Double = 0.0,
    val vehicleInfo: String = ""
)
