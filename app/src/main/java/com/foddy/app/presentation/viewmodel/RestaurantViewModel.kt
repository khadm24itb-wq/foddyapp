package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.Restaurant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed interface RestaurantUiState {
    object Loading : RestaurantUiState
    data class Success(
        val restaurants: List<Restaurant>,
        val isLastPage: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : RestaurantUiState
    data class Error(val message: String) : RestaurantUiState
}

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow<RestaurantUiState>(RestaurantUiState.Loading)
    val uiState: StateFlow<RestaurantUiState> = _uiState

    // Keep internal state for pagination
    private var allRestaurants = mutableListOf<Restaurant>()
    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private var isCurrentlyLoading = false

    init {
        loadMoreRestaurants()
    }

    fun loadMoreRestaurants() {
        if (isCurrentlyLoading || isLastPage) return

        viewModelScope.launch {
            isCurrentlyLoading = true
            
            // Update UI to show loading more if we already have data
            val currentState = _uiState.value
            if (currentState is RestaurantUiState.Success) {
                _uiState.value = currentState.copy(isLoadingMore = true)
            }

            try {
                var query = firestore.collection("restaurants")
                    .orderBy("rating", Query.Direction.DESCENDING)
                    .limit(10)

                if (lastVisible != null) {
                    query = query.startAfter(lastVisible!!)
                }

                val snapshot = query.get().await()
                if (snapshot.isEmpty) {
                    isLastPage = true
                } else {
                    val newRestaurants = snapshot.toObjects(Restaurant::class.java)
                    allRestaurants.addAll(newRestaurants)
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                }
                
                _uiState.value = RestaurantUiState.Success(
                    restaurants = allRestaurants.toList(),
                    isLastPage = isLastPage,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                if (allRestaurants.isEmpty()) {
                    _uiState.value = RestaurantUiState.Error(e.message ?: "Unknown Error")
                } else {
                    // If we have data, just stop the loading more indicator
                    _uiState.value = RestaurantUiState.Success(
                        restaurants = allRestaurants.toList(),
                        isLastPage = isLastPage,
                        isLoadingMore = false
                    )
                }
            } finally {
                isCurrentlyLoading = false
            }
        }
    }

    fun refresh() {
        lastVisible = null
        isLastPage = false
        allRestaurants.clear()
        _uiState.value = RestaurantUiState.Loading
        loadMoreRestaurants()
    }
}
