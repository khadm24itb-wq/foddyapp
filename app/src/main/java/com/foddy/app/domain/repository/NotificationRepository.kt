package com.foddy.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    
    // Gửi thông báo cho Chủ quán
    suspend fun sendNewOrderNotification(
        restaurantId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit>
    
    // Gửi thông báo cho Shipper
    suspend fun sendNewDeliveryNotification(
        shipperId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit>
    
    // Gửi thông báo cho Khách hàng
    suspend fun sendOrderStatusNotification(
        userId: String,
        orderId: String,
        event: OrderEvent
    ): Result<Unit>
    
    // Gửi thông báo đã thanh toán cho Chủ quán
    suspend fun sendPaymentConfirmedNotification(
        restaurantId: String,
        orderId: String,
        amount: Long
    ): Result<Unit>
    
    // Lắng nghe thông báo
    fun observeNotifications(): Flow<List<Notification>>
    
    // Đánh dấu đã đọc
    suspend fun markAsRead(notificationId: String)
}

data class OrderInfo(
    val orderId: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Long,
    val deliveryAddress: String,
    val paymentMethod: String,
    val notes: String? = null
)

sealed class OrderEvent {
    data object OrderConfirmed : OrderEvent()
    data object Preparing : OrderEvent()
    data object ReadyForPickup : OrderEvent()
    data object Delivering : OrderEvent()
    data class Delivered(val shipperName: String) : OrderEvent()
    data class Cancelled(val reason: String) : OrderEvent()
}

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val orderId: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false,
    val data: Map<String, String> = emptyMap()
)

enum class NotificationType {
    NEW_ORDER,
    ORDER_STATUS,
    PAYMENT_CONFIRMED,
    NEW_DELIVERY,
    DELIVERY_UPDATE,
    REVIEW,
    PROMOTION,
    GENERAL
}
