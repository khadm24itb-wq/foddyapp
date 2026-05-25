package com.foddy.app.data.repository

import com.foddy.app.data.local.FoodItemDao
import com.foddy.app.data.mapper.toDomain
import com.foddy.app.data.mapper.toEntity
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.MenuRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val foodItemDao: FoodItemDao
) : MenuRepository {
    private val db = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getMenuItems(): Flow<List<FoodItem>> = callbackFlow {
        // Trả về dữ liệu từ local trước nếu có
        scope.launch {
            val localItems = foodItemDao.getAllItems().map { it.toDomain() }
            if (localItems.isNotEmpty()) {
                trySend(localItems)
            }
        }

        val listener = db.collection("menu").addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
                }
                
                // Cập nhật local database
                scope.launch {
                    foodItemDao.clearAll()
                    foodItemDao.insertAll(items.map { it.toEntity() })
                }

                trySend(items)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addMenuItem(item: FoodItem) {
        val itemData = item.copy(id = "")
        db.collection("menu").add(itemData).await()
    }

    override suspend fun removeMenuItem(item: FoodItem) {
        db.collection("menu").document(item.id).delete().await()
    }
}
