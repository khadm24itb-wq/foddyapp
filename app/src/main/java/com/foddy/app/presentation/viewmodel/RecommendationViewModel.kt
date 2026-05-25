package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.foddy.app.BuildConfig

data class Message(val text: String, val isUser: Boolean)

@HiltViewModel
class RecommendationViewModel @Inject constructor() : ViewModel() {
    private val _recommendations = MutableStateFlow<List<FoodItem>>(emptyList())
    val recommendations: StateFlow<List<FoodItem>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Message>>(emptyList())
    val chatHistory: StateFlow<List<Message>> = _chatHistory.asStateFlow()

    // Sử dụng Gemini AI từ BuildConfig
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun sendMessage(text: String, allItems: List<FoodItem>) {
        if (text.isBlank()) return

        val userMessage = Message(text, true)
        _chatHistory.value = _chatHistory.value + userMessage
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = """
                    Bạn là một trợ lý ảo của ứng dụng Foddy, chuyên gợi ý món ăn.
                    Người dùng hỏi: "$text".
                    Danh sách món ăn có sẵn: ${allItems.joinToString { it.name }}.
                    Hãy trả lời thân thiện và gợi ý các món phù hợp từ danh sách trên nếu có thể.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val aiResponse = response.text ?: "Xin lỗi, tôi không thể xử lý yêu cầu này."
                _chatHistory.value = _chatHistory.value + Message(aiResponse, false)
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + Message("Lỗi kết nối AI: ${e.message}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAIRecommendations(userPreferences: String, allItems: List<FoodItem>) {
        if (_recommendations.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = """
                    Dựa trên sở thích của người dùng: "$userPreferences".
                    Hãy chọn ra tối đa 3 món ăn phù hợp nhất từ danh sách sau:
                    ${allItems.joinToString { it.name }}
                    Trả về CHỈ tên các món ăn, cách nhau bởi dấu phẩy.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val recommendedNames = response.text?.split(",")?.map { it.trim() } ?: emptyList()
                
                _recommendations.value = allItems.filter { item -> 
                    recommendedNames.any { it.contains(item.name, ignoreCase = true) }
                }
            } catch (e: Exception) {
                // Fallback nếu AI lỗi: Lấy ngẫu nhiên 2 món
                _recommendations.value = allItems.shuffled().take(2)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
