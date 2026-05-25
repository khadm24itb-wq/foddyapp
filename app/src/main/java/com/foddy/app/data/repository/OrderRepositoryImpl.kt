package com.foddy.app.data.repository

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor() : OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")

    override fun getPendingOrders(): Flow<List<OrderRequest>> = callbackFlow {
        val subscription = ordersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { it.toObject(OrderRequest::class.java)?.copy(id = it.id) }
                trySend(orders)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getOrderById(orderId: String): Flow<OrderRequest?> = callbackFlow {
        val subscription = ordersCollection.document(orderId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val order = snapshot.toObject(OrderRequest::class.java)?.copy(id = snapshot.id)
                trySend(order)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun placeOrder(order: OrderRequest) {
        ordersCollection.document(order.id).set(order).await()
    }

    override suspend fun updateOrderStatus(orderId: String, status: String) {
        val timestampKey = when (status) {
            "accepted" -> "timestamps.acceptedAt"
            "delivering" -> "timestamps.deliveringAt"
            "completed" -> "timestamps.completedAt"
            else -> null
        }
        val updates = mutableMapOf<String, Any>("status" to status)
        timestampKey?.let { updates[it] = System.currentTimeMillis() }
        ordersCollection.document(orderId).update(updates).await()
    }

    override suspend fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        val updates = mapOf(
            "status" to "accepted",
            "driverId" to driverId,
            "driverName" to driverName,
            "timestamps.acceptedAt" to System.currentTimeMillis()
        )
        ordersCollection.document(orderId).update(updates).await()
    }

    override suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double) {
        ordersCollection.document(orderId)
            .update("driverLocation", mapOf("lat" to lat, "lng" to lng))
            .await()
    }
}
