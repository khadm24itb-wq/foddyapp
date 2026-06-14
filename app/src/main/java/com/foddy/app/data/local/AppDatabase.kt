package com.foddy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.foddy.app.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FoodItemEntity::class,
        CartItemEntity::class,
        RestaurantEntity::class,
        OrderEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun cartDao(): CartDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun orderDao(): OrderDao
}
