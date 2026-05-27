package com.foddy.app.data.repository

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override fun getPendingOrders(): Flow<List<OrderRequest>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereIn("status", listOf("pending", "accepted", "preparing"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to pending orders")
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    override fun getOrdersByRestaurant(restaurantId: String): Flow<List<OrderRequest>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to restaurant orders")
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    override fun getOrdersByUser(userId: String): Flow<List<OrderRequest>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to user orders")
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    override fun getOrderById(orderId: String): Flow<OrderRequest?> = callbackFlow {
        val listener = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to order $orderId")
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(OrderRequest::class.java)?.copy(id = snapshot.id)
                trySend(order)
            }
        awaitClose { listener.remove() }
    }

    override fun trackDriverLocation(orderId: String): Flow<DriverLocation?> = callbackFlow {
        val listener = firestore.collection("driver_locations").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error tracking driver location for order $orderId")
                    return@addSnapshotListener
                }
                val location = snapshot?.toObject(DriverLocation::class.java)
                trySend(location)
            }
        awaitClose { listener.remove() }
    }

    override fun getChatMessages(orderId: String): Flow<List<OrderChatMessage>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereEqualTo("orderId", orderId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to chats for order $orderId")
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(OrderChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun placeOrder(order: OrderRequest): Result<String> = try {
        val docRef = firestore.collection("orders").document()
        val orderWithId = order.copy(id = docRef.id, createdAt = System.currentTimeMillis())
        docRef.set(orderWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "Error placing order")
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = try {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "timestamps.$status" to System.currentTimeMillis()
        )
        firestore.collection("orders").document(orderId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating order status")
        Result.failure(e)
    }

    override suspend fun acceptOrder(orderId: String, driverId: String, driverName: String): Result<Unit> = try {
        val updates = mapOf(
            "status" to "delivering",
            "driverId" to driverId,
            "driverName" to driverName,
            "timestamps.delivering" to System.currentTimeMillis()
        )
        firestore.collection("orders").document(orderId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error accepting order")
        Result.failure(e)
    }

    override suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double): Result<Unit> = try {
        val location = DriverLocation(orderId, lat, lng, System.currentTimeMillis())
        firestore.collection("driver_locations").document(orderId).set(location).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating driver location")
        Result.failure(e)
    }

    override suspend fun sendChatMessage(message: OrderChatMessage): Result<Unit> = try {
        firestore.collection("chats").add(message).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending chat message")
        Result.failure(e)
    }
}
