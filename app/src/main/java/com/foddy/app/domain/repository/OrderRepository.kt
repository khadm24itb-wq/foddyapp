package com.foddy.app.domain.repository

import com.foddy.app.domain.model.OrderRequest
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getPendingOrders(): Flow<List<OrderRequest>>
    fun getOrderById(orderId: String): Flow<OrderRequest?>
    suspend fun placeOrder(order: OrderRequest)
    suspend fun updateOrderStatus(orderId: String, status: String)
    suspend fun acceptOrder(orderId: String, driverId: String, driverName: String)
    suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double)
}
