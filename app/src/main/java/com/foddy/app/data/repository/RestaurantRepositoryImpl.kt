package com.foddy.app.data.repository

import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.repository.RestaurantRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : RestaurantRepository {

    override fun getRestaurants(limit: Long, lastVisible: DocumentSnapshot?): Flow<Pair<List<Restaurant>, DocumentSnapshot?>> = callbackFlow {
        var query = db.collection("restaurants")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        val listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Restaurant::class.java)
                val lastDoc = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                trySend(list to lastDoc)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getRestaurantById(id: String): Restaurant? {
        return try {
            val doc = db.collection("restaurants").document(id).get().await()
            doc.toObject(Restaurant::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
