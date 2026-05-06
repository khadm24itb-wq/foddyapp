package com.example.foodle.ui

import androidx.lifecycle.ViewModel
import com.example.foodle.model.CartItem
import com.example.foodle.model.Driver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@IgnoreExtraProperties
data class OrderRequest(
    val id: String = "",
    val customerId: String = "",
    val restaurantId: String = "rest_001", // Mặc định cho bản demo
    val restaurantName: String = "",
    val address: String = "",
    val totalAmount: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    var status: String = "pending", // pending, accepted, delivering, completed, cancelled
    var driverId: String? = null,
    var driverName: String? = null,
    var driverLocation: Map<String, Double>? = null, // { "lat": 21.0, "lng": 105.0 }
    val timestamps: Map<String, Long> = mapOf("placedAt" to System.currentTimeMillis())
)

class OrderViewModel : ViewModel() {
    private val _pendingOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val pendingOrders: StateFlow<List<OrderRequest>> = _pendingOrders.asStateFlow()

    private val _currentOrder = MutableStateFlow<OrderRequest?>(null)
    val currentOrder: StateFlow<OrderRequest?> = _currentOrder.asStateFlow()

    init {
        listenToOrders()
    }

    private fun listenToOrders() {
        try {
            val db = FirebaseFirestore.getInstance()
            // Lắng nghe toàn bộ thay đổi trong collection "orders"
            db.collection("orders")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        println("Firestore Error: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                        }
                        // Cập nhật danh sách đơn hàng cho tài xế
                        _pendingOrders.value = orders
                        
                        // Cập nhật trạng thái đơn hàng hiện tại cho Khách hàng
                        _currentOrder.value?.let { current ->
                            val updated = orders.find { it.id == current.id }
                            if (updated != null) {
                                _currentOrder.value = updated
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            println("Firebase not available: ${e.message}")
        }
    }

    fun placeOrder(order: OrderRequest, userId: String) {
        val finalOrder = order.copy(
            id = UUID.randomUUID().toString().substring(0, 8),
            customerId = userId
        )
        _currentOrder.value = finalOrder
        try {
            FirebaseFirestore.getInstance().collection("orders")
                .document(finalOrder.id)
                .set(finalOrder)
        } catch (e: Exception) {
            println("Offline mode: Order saved locally")
        }
    }

    fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        try {
            val updates = mapOf(
                "status" to "accepted",
                "driverId" to driverId,
                "driverName" to driverName,
                "timestamps.acceptedAt" to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update(updates)
        } catch (e: Exception) {
            println("Error accepting order: ${e.message}")
        }
    }

    // Cập nhật vị trí tài xế (Real-time tracking optimization)
    fun updateDriverLocation(orderId: String, lat: Double, lng: Double) {
        FirebaseFirestore.getInstance().collection("orders").document(orderId)
            .update("driverLocation", mapOf("lat" to lat, "lng" to lng))
    }

    fun updateOrderStatus(orderId: String, status: String) {
        val timestampKey = when(status) {
            "accepted" -> "timestamps.acceptedAt"
            "delivering" -> "timestamps.deliveringAt"
            "completed" -> "timestamps.completedAt"
            else -> null
        }
        val updates = mutableMapOf<String, Any>("status" to status)
        timestampKey?.let { updates[it] = System.currentTimeMillis() }
        
        FirebaseFirestore.getInstance().collection("orders").document(orderId)
            .update(updates)
    }
}
