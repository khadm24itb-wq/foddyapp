package com.foddy.app.data.repository

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.MenuRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor() : MenuRepository {
    private val db = FirebaseFirestore.getInstance()

    override fun getMenuItems(): Flow<List<FoodItem>> = callbackFlow {
        val listener = db.collection("menu").addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
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
