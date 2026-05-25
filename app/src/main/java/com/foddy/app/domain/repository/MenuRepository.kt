package com.foddy.app.domain.repository

import com.foddy.app.domain.model.FoodItem
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getMenuItems(): Flow<List<FoodItem>>
    suspend fun addMenuItem(item: FoodItem)
    suspend fun removeMenuItem(item: FoodItem)
}
