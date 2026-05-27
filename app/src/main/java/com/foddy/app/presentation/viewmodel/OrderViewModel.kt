package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.core.location.DriverLocationManager
import android.location.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val locationManager: DriverLocationManager
) : ViewModel() {

    private val _userOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val userOrders: StateFlow<List<OrderRequest>> = _userOrders.asStateFlow()

    private val _restaurantOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val restaurantOrders: StateFlow<List<OrderRequest>> = _restaurantOrders.asStateFlow()

    private val _pendingOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val pendingOrders: StateFlow<List<OrderRequest>> = _pendingOrders.asStateFlow()

    private val _currentOrder = MutableStateFlow<OrderRequest?>(null)
    val currentOrder: StateFlow<OrderRequest?> = _currentOrder.asStateFlow()

    private val _driverLocation = MutableStateFlow<DriverLocation?>(null)
    val driverLocation: StateFlow<DriverLocation?> = _driverLocation.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<OrderChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<OrderChatMessage>> = _chatMessages.asStateFlow()

    fun placeOrder(order: OrderRequest, onComplete: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.placeOrder(order)
            onComplete(result)
        }
    }

    fun listenToUserOrders(userId: String) {
        viewModelScope.launch {
            orderRepository.getOrdersByUser(userId).collect {
                _userOrders.value = it
            }
        }
    }

    fun listenToRestaurantOrders(restaurantId: String) {
        viewModelScope.launch {
            orderRepository.getOrdersByRestaurant(restaurantId).collect {
                _restaurantOrders.value = it
            }
        }
    }

    fun listenToPendingOrders() {
        viewModelScope.launch {
            orderRepository.getPendingOrders().collect {
                _pendingOrders.value = it
            }
        }
    }

    fun trackOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrderById(orderId).collect { order ->
                _currentOrder.value = order
                if (order?.status != "PENDING" && order?.id != null) {
                    trackDriverLocation(order.id)
                }
            }
        }
        viewModelScope.launch {
            orderRepository.getChatMessages(orderId).collect {
                _chatMessages.value = it
            }
        }
    }

    private fun trackDriverLocation(orderId: String) {
        viewModelScope.launch {
            orderRepository.trackDriverLocation(orderId).collect {
                _driverLocation.value = it
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
        }
    }

    fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        viewModelScope.launch {
            orderRepository.acceptOrder(orderId, driverId, driverName)
        }
    }

    fun updateLocation(orderId: String, lat: Double, lng: Double) {
        val newLocation = Location("").apply {
            latitude = lat
            longitude = lng
        }

        if (locationManager.shouldUpdateLocation(newLocation)) {
            viewModelScope.launch {
                orderRepository.updateDriverLocation(orderId, lat, lng)
            }
        }
    }

    fun sendChatMessage(message: String, senderId: String, receiverId: String, orderId: String) {
        viewModelScope.launch {
            val chatMsg = OrderChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                orderId = orderId,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            orderRepository.sendChatMessage(chatMsg)
        }
    }

    private var simulationJob: kotlinx.coroutines.Job? = null

    fun startLocationSimulation(orderId: String) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val startLat = 21.0285
            val startLng = 105.8542
            val endLat = 21.0333
            val endLng = 105.8444
            
            var steps = 0
            val totalSteps = 20
            
            while (steps <= totalSteps) {
                val currentLat = startLat + (endLat - startLat) * (steps.toDouble() / totalSteps)
                val currentLng = startLng + (endLng - startLng) * (steps.toDouble() / totalSteps)
                
                updateLocation(orderId, currentLat, currentLng)
                
                steps++
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    fun stopLocationSimulation() {
        simulationJob?.cancel()
    }
}
