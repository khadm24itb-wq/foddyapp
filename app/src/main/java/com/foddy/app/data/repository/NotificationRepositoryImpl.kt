package com.foddy.app.data.repository

import com.foddy.app.domain.repository.Notification
import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.NotificationType
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.repository.OrderInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : NotificationRepository {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    
    override suspend fun sendNewOrderNotification(
        restaurantId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit> = runCatching {
        firestore.collection("notifications")
            .add(
                hashMapOf(
                    "restaurantId" to restaurantId,
                    "orderId" to orderId,
                    "title" to "🆕 Đơn hàng mới!",
                    "body" to "${orderInfo.customerName} đặt ${orderInfo.items.joinToString(", ")} - ${orderInfo.totalAmount.formatVND()}",
                    "type" to NotificationType.NEW_ORDER.name,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            .await()
        
        addLocalNotification(
            Notification(
                id = orderId,
                title = "🆕 Đơn hàng mới!",
                body = "${orderInfo.customerName} đặt ${orderInfo.items.firstOrNull() ?: "món ăn"}...",
                type = NotificationType.NEW_ORDER,
                orderId = orderId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun sendPaymentConfirmedNotification(
        restaurantId: String,
        orderId: String,
        amount: Long
    ): Result<Unit> = runCatching {
        firestore.collection("notifications")
            .add(
                hashMapOf(
                    "restaurantId" to restaurantId,
                    "orderId" to orderId,
                    "title" to "💰 Đã thanh toán!",
                    "body" to "Đơn #$orderId đã thanh toán ${amount.formatVND()}",
                    "type" to NotificationType.PAYMENT_CONFIRMED.name,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            .await()
        
        addLocalNotification(
            Notification(
                id = "pay_$orderId",
                title = "💰 Đã thanh toán!",
                body = "Đơn #$orderId đã thanh toán ${amount.formatVND()}",
                type = NotificationType.PAYMENT_CONFIRMED,
                orderId = orderId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun sendNewDeliveryNotification(
        shipperId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit> = runCatching {
        addLocalNotification(
            Notification(
                id = "del_$orderId",
                title = "🚴 Đơn hàng mới cần giao!",
                body = "Giao đến ${orderInfo.deliveryAddress}",
                type = NotificationType.NEW_DELIVERY,
                orderId = orderId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun sendOrderStatusNotification(
        userId: String,
        orderId: String,
        event: OrderEvent
    ): Result<Unit> = runCatching {
        val (title, body) = when (event) {
            is OrderEvent.OrderConfirmed -> "✅ Đơn hàng đã xác nhận" to "Quán đang chuẩn bị món cho bạn"
            is OrderEvent.Preparing -> "👨‍🍳 Đang chuẩn bị" to "Đơn hàng đang được nấu"
            is OrderEvent.ReadyForPickup -> "🍽️ Sẵn sàng!" to "Đơn hàng sẵn sàng, đang chờ Shipper"
            is OrderEvent.Delivering -> "🚴 Đang giao" to "Shipper đang trên đường giao cho bạn"
            is OrderEvent.Delivered -> "✅ Giao hàng thành công" to "Cảm ơn bạn đã đặt FoddyApp!"
            is OrderEvent.Cancelled -> "❌ Đơn hàng đã hủy" to event.reason
        }
        
        addLocalNotification(
            Notification(
                id = "status_$orderId",
                title = title,
                body = body,
                type = NotificationType.ORDER_STATUS,
                orderId = orderId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    override fun observeNotifications(): Flow<List<Notification>> = _notifications.asStateFlow()
    
    override suspend fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }
    
    private fun addLocalNotification(notification: Notification) {
        _notifications.value = listOf(notification) + _notifications.value
    }
    
    private fun Long.formatVND() = 
        java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(this) + " đ"
}
