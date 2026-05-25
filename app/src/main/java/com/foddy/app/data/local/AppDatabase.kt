package com.foddy.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.foddy.app.data.model.UserEntity

@Database(entities = [UserEntity::class, FoodItemEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodItemDao(): FoodItemDao
}
