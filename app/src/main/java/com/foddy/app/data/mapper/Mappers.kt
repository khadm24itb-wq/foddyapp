package com.foddy.app.data.mapper

import com.foddy.app.data.local.FoodItemEntity
import com.foddy.app.domain.model.FoodItem

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
