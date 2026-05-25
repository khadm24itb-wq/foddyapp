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

@HiltViewModel
class RecommendationViewModel @Inject constructor() : ViewModel() {
    private val _recommendations = MutableStateFlow<List<FoodItem>>(emptyList())
    val recommendations: StateFlow<List<FoodItem>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // S? d?ng Gemini AI d? g?i ý món an (C?n API Key th?c t? t? Google AI Studio)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "YOUR_GEMINI_API_KEY_HERE" 
    )

    fun getAIRecommendations(userPreferences: String, allItems: List<FoodItem>) {
        if (_recommendations.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = """
                    D?a trên s? thích c?a ngu?i dùng: "$userPreferences".
                    Hãy ch?n ra t?i da 3 món an phù h?p nh?t t? danh sách sau:
                    ${allItems.joinToString { it.name }}
                    Tr? v? CH? tên các món an, cách nhau b?i d?u ph?y.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val recommendedNames = response.text?.split(",")?.map { it.trim() } ?: emptyList()
                
                _recommendations.value = allItems.filter { item -> 
                    recommendedNames.any { it.contains(item.name, ignoreCase = true) }
                }
            } catch (e: Exception) {
                // Fallback n?u AI l?i ho?c chua có API Key: L?y ng?u nhiên 2 món
                _recommendations.value = allItems.shuffled().take(2)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
