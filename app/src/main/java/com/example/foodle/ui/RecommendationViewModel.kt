package com.example.foodle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodle.model.FoodItem
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel : ViewModel() {
    private val _recommendations = MutableStateFlow<List<FoodItem>>(emptyList())
    val recommendations: StateFlow<List<FoodItem>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Sử dụng Gemini AI để gợi ý món ăn (Cần API Key thực tế)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "YOUR_GEMINI_API_KEY" // Người dùng cần điền API key ở đây
    )

    fun getAIRecommendations(userPreferences: String, allItems: List<FoodItem>) {
        if (_recommendations.value.isNotEmpty()) return // Tránh gọi lại nhiều lần

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = """
                    Dựa trên sở thích của người dùng: "$userPreferences".
                    Hãy chọn ra tối đa 3 món ăn phù hợp nhất từ danh sách sau:
                    ${allItems.joinToString { it.name }}
                    Trả về chỉ tên các món ăn, cách nhau bởi dấu phẩy.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val recommendedNames = response.text?.split(",")?.map { it.trim() } ?: emptyList()
                
                _recommendations.value = allItems.filter { item -> 
                    recommendedNames.any { it.contains(item.name, ignoreCase = true) }
                }
            } catch (e: Exception) {
                // Fallback nếu AI lỗi: Lấy đại 2 món
                _recommendations.value = allItems.shuffled().take(2)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
