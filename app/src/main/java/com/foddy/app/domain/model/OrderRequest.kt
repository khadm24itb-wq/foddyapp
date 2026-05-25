package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class OrderRequest(
    val id: String = "",
    val customerId: String = "",
    val restaurantId: String = "rest_001", // Mặc định cho bản demo
    val restaurantName: String = "",
    val address: String = "",
    val totalAmount: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    val paymentMethod: String = "Tiền mặt",
    var status: String = "pending", // pending, accepted, delivering, completed, cancelled
    var driverId: String? = null,
    var driverName: String? = null,
    var driverLocation: Map<String, Double>? = null, // { "lat": 21.0, "lng": 105.0 }
    val timestamps: Map<String, Long> = mapOf("placedAt" to System.currentTimeMillis())
)
