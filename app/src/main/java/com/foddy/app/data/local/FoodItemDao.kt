package com.foddy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Query("SELECT * FROM food_items")
    suspend fun getAllItems(): List<FoodItemEntity>

    @Query("DELETE FROM food_items")
    suspend fun clearAll()
}
