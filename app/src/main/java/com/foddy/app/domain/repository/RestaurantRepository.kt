package com.foddy.app.domain.repository

import com.foddy.app.domain.model.Restaurant
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun getRestaurants(limit: Long, lastVisible: DocumentSnapshot?): Flow<Pair<List<Restaurant>, DocumentSnapshot?>>
    suspend fun getRestaurantById(id: String): Restaurant?
}
