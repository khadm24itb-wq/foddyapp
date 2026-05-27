package com.foddy.app.domain.repository

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getMenuItems(restaurantId: String? = null): Flow<List<FoodItem>>
    suspend fun addMenuItem(item: FoodItem)
    suspend fun removeMenuItem(item: FoodItem)
    fun getMenuItemsWithCache(restaurantId: String? = null): Flow<Resource<List<FoodItem>>>
}
