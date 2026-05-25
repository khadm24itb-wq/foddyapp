package com.foddy.app.data.local

import com.foddy.app.data.model.UserEntity
import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET name = :newName WHERE email = :email")
    suspend fun updateName(email: String, newName: String)

    @Query("SELECT * FROM users LIMIT :pageSize OFFSET :offset")
    suspend fun getUsersPaginated(pageSize: Int, offset: Int): List<UserEntity>
}
