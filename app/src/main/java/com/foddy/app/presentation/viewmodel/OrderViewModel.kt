package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.core.location.DriverLocationManager
import android.location.Location
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val locationManager: DriverLocationManager
) : ViewModel() {

    private val _orderState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val orderState: StateFlow<UiState<String>> = _orderState.asStateFlow()

    private val _userOrders = MutableStateFlow<UiState<List<OrderRequest>>>(UiState.Idle)
    val userOrders: StateFlow<UiState<List<OrderRequest>>> = _userOrders.asStateFlow()

    private val _restaurantOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val restaurantOrders: StateFlow<List<OrderRequest>> = _restaurantOrders.asStateFlow()

    private val _pendingOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val pendingOrders: StateFlow<List<OrderRequest>> = _pendingOrders.asStateFlow()

    private val _orderIdFlow = MutableStateFlow<String?>(null)

    val currentOrder: StateFlow<OrderRequest?> = _orderIdFlow
        .filterNotNull()
        .flatMapLatest { id -> orderRepository.getOrderById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<OrderChatMessage>> = _orderIdFlow
        .filterNotNull()
        .flatMapLatest { id -> orderRepository.getChatMessages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driverLocation: StateFlow<DriverLocation?> = currentOrder
        .flatMapLatest { order ->
            if (order != null && order.status != "PENDING") {
                orderRepository.trackDriverLocation(order.id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun trackOrder(orderId: String) {
        _orderIdFlow.value = orderId
    }

    fun listenToUserOrders(userId: String) {
        viewModelScope.launch {
            _userOrders.value = UiState.Loading
            orderRepository.getOrdersByUser(userId)
                .catch { e ->
                    _userOrders.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect {
                    _userOrders.value = UiState.Success(it)
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

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
        }
    }

    fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        viewModelScope.launch {
            orderRepository.acceptOrder(orderId, driverId, driverName, "PREPARING")
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
