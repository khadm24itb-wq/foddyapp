package com.example.foodle.ui

import androidx.lifecycle.ViewModel
import com.example.foodle.model.FoodItem
import com.example.foodle.data.DummyData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel : ViewModel() {
    private val _foodItems = MutableStateFlow<List<FoodItem>>(DummyData.restaurants[0].menu)
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    init {
        listenToMenuChanges()
    }

    private fun listenToMenuChanges() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("menu").addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Lỗi lắng nghe Firestore: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        // Sử dụng toObject để tự động map dữ liệu, an toàn và ít lỗi hơn
                        val item = doc.toObject(FoodItem::class.java)
                        item?.copy(id = doc.id) // Gán lại ID từ document của Firebase
                    }
                    if (items.isNotEmpty()) {
                        _foodItems.value = items
                    }
                }
            }
        } catch (e: Exception) {
            println("Firebase chưa được cấu hình hoặc lỗi khởi tạo: ${e.message}")
        }
    }

    fun addFoodItem(item: FoodItem) {
        try {
            val db = FirebaseFirestore.getInstance()
            // Xóa ID để Firestore tự tạo ID ngẫu nhiên, tránh trùng lặp
            val itemData = item.copy(id = "") 
            db.collection("menu").add(itemData)
                .addOnSuccessListener { println("Thêm món thành công") }
                .addOnFailureListener { e -> println("Lỗi thêm món: ${e.message}") }
        } catch (e: Exception) {
            // Fallback nếu không có firebase (chạy offline)
            _foodItems.value = _foodItems.value + item
        }
    }

    fun removeFoodItem(item: FoodItem) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("menu").document(item.id).delete()
                .addOnSuccessListener { println("Xóa món thành công") }
        } catch (e: Exception) {
            _foodItems.value = _foodItems.value.filter { it.id != item.id }
        }
    }
}
