package com.foddy.app.data

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Restaurant

object DummyData {
    val foodItems = listOf(
        FoodItem(
            id = "1", 
            name = "Phở Bò", 
            description = "Phở bò truyền thống với nước dùng đậm đà", 
            price = 55000.0, 
            discountPrice = 45000.0, 
            image = "https://images.unsplash.com/photo-1582878826629-29b7ad1ccd28?w=500", 
            rating = 4.8, 
            calories = 450, 
            isFlashSale = true,
            restaurantId = "rest_001",
            category = "Món Việt"
        ),
        FoodItem(
            id = "2", 
            name = "Bún Chả", 
            description = "Bún chả Hà Nội nướng than hoa", 
            price = 45000.0, 
            image = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", 
            rating = 4.7, 
            calories = 500,
            restaurantId = "rest_001",
            category = "Món Việt"
        ),
        FoodItem(
            id = "3", 
            name = "Bánh Mì", 
            description = "Bánh mì kẹp thịt đầy đủ topping", 
            price = 25000.0, 
            discountPrice = 15000.0, 
            image = "https://images.unsplash.com/photo-1600454021970-351eff4a6554?w=500", 
            rating = 4.5, 
            calories = 350, 
            isFlashSale = true,
            restaurantId = "rest_001",
            category = "Món Việt"
        ),
        FoodItem(
            id = "4", 
            name = "Cơm Tấm", 
            description = "Cơm tấm sườn bì chả đặc biệt", 
            price = 40000.0, 
            image = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500", 
            rating = 4.6, 
            calories = 600,
            restaurantId = "rest_001",
            category = "Món Việt"
        ),
        FoodItem(
            id = "5", 
            name = "Pizza Hải Sản", 
            description = "Pizza với tôm, mực và phô mai", 
            price = 150000.0, 
            discountPrice = 99000.0, 
            image = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500", 
            rating = 4.9, 
            calories = 800, 
            isFlashSale = true,
            restaurantId = "rest_002",
            category = "Pizza"
        ),
        FoodItem(
            id = "6", 
            name = "Sushi Nigiri", 
            description = "Sushi cá hồi tươi ngon từ Nhật Bản", 
            price = 120000.0, 
            image = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500", 
            rating = 4.9, 
            calories = 120,
            restaurantId = "rest_003",
            category = "Sushi"
        ),
        FoodItem(
            id = "7", 
            name = "Trà Sữa Thái", 
            description = "Trà sữa thái xanh mát lạnh", 
            price = 35000.0, 
            discountPrice = 25000.0, 
            image = "https://images.unsplash.com/photo-1544145945-f904253d0c7b?w=500", 
            rating = 4.7, 
            calories = 1000, 
            isFlashSale = true,
            restaurantId = "rest_001",
            category = "Đồ uống"
        )
    )

    val restaurants = listOf(
        Restaurant(
            id = "rest_001",
            name = "Hà Nội Quán",
            address = "123 Phố Huế, Hà Nội",
            image = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=500",
            rating = 4.8,
            reviewCount = 120,
            category = "Món Việt",
            deliveryTime = "20-30 min",
            shippingFee = 15000.0,
            lat = 21.0123,
            lng = 105.8521
        ),
        Restaurant(
            id = "rest_002",
            name = "The Pizza Company",
            address = "456 Cầu Giấy, Hà Nội",
            image = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=500",
            rating = 4.5,
            reviewCount = 85,
            category = "Pizza",
            deliveryTime = "30-45 min",
            shippingFee = 20000.0,
            lat = 21.0367,
            lng = 105.7831
        ),
        Restaurant(
            id = "rest_003",
            name = "Sushi World",
            address = "789 Kim Mã, Hà Nội",
            image = "https://images.unsplash.com/photo-1579027989536-b7b1f875659b?w=500",
            rating = 4.9,
            reviewCount = 210,
            category = "Sushi",
            deliveryTime = "25-40 min",
            shippingFee = 25000.0,
            lat = 21.0289,
            lng = 105.8123
        )
    )
    
    val categories = listOf(
        Category("1", "Tất cả", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200"),
        Category("2", "Món Việt", "https://images.unsplash.com/photo-1582878826629-29b7ad1ccd28?w=200"),
        Category("3", "Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200"),
        Category("4", "Sushi", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=200"),
        Category("5", "Đồ uống", "https://images.unsplash.com/photo-1544145945-f904253d0c7b?w=200")
    )
}

data class Category(val id: String, val name: String, val imageUrl: String)
