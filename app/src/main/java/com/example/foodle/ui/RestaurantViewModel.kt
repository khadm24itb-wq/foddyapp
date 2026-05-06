package com.example.foodle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodle.model.Restaurant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false

    init {
        loadMoreRestaurants()
    }

    fun loadMoreRestaurants() {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var query = db.collection("restaurants")
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
                    _restaurants.value = _restaurants.value + newRestaurants
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                }
            } catch (e: Exception) {
                println("Error loading restaurants: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        lastVisible = null
        isLastPage = false
        _restaurants.value = emptyList()
        loadMoreRestaurants()
    }
}
