package com.foddy.app.data

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Restaurant

object DummyData {
    val foodItems = listOf(
        FoodItem(
            "1", "Phở Bò", "Phở bò truyền thống với nước dùng đậm đà", 
            55000.0, 45000.0, 
            "https://images.unsplash.com/photo-1582878826629-29b7ad1ccd28?w=500", 
            4.8, 450, isFlashSale = true
        ),
        FoodItem(
            "2", "Bún Chả", "Bún chả Hà Nội nướng than hoa", 
            45000.0, null, 
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", 
            4.7, 500
        ),
        FoodItem(
            "3", "Bánh Mì", "Bánh mì kẹp thịt đầy đủ topping", 
            25000.0, 15000.0, 
            "https://images.unsplash.com/photo-1600454021970-351eff4a6554?w=500", 
            4.5, 350, isFlashSale = true
        ),
        FoodItem(
            "4", "Cơm Tấm", "Cơm tấm sườn bì chả đặc biệt", 
            40000.0, null, 
            "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500", 
            4.6, 600
        ),
        FoodItem(
            "5", "Pizza Hải Sản", "Pizza với tôm, mực và phô mai", 
            150000.0, 99000.0, 
            "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500", 
            4.9, 800, isFlashSale = true
        )
    )

    val restaurants = listOf(
        Restaurant(
            "1", "Hà Nội Quán", "Chuyên các món đặc sản miền Bắc", 
            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=500", 
            4.8, "20-30 min", 15000.0, foodItems, "Món Việt"
        ),
        Restaurant(
            "2", "The Pizza Company", "Pizza phong cách Ý", 
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=500", 
            4.5, "30-45 min", 20000.0, listOf(foodItems[4]), "Fast Food"
        ),
        Restaurant(
            "3", "Sushi World", "Sushi và Sashimi tươi ngon", 
            "https://images.unsplash.com/photo-1579027989536-b7b1f875659b?w=500", 
            4.9, "25-40 min", 25000.0, foodItems, "Món Nhật"
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
