package com.foddy.app.data.mapper

import com.foddy.app.data.local.FoodItemEntity
import com.foddy.app.data.model.UserEntity
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.User

fun FoodItem.toEntity(): FoodItemEntity {
    return FoodItemEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        discountPrice = discountPrice,
        imageRes = imageRes,
        rating = rating,
        calories = calories,
        isFlashSale = isFlashSale
    )
}

fun FoodItemEntity.toDomain(): FoodItem {
    return FoodItem(
        id = id,
        name = name,
        description = description,
        price = price,
        discountPrice = discountPrice,
        imageRes = imageRes,
        rating = rating,
        calories = calories,
        isFlashSale = isFlashSale
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        address = address,
        profilePictureUrl = profilePictureUrl,
        role = role
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        address = address,
        profilePictureUrl = profilePictureUrl,
        role = role,
        isLoggedIn = true
    )
}
