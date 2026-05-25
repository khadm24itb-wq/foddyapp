package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _pendingOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val pendingOrders: StateFlow<List<OrderRequest>> = _pendingOrders.asStateFlow()

    private val _currentOrder = MutableStateFlow<OrderRequest?>(null)
    val currentOrder: StateFlow<OrderRequest?> = _currentOrder.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.getPendingOrders().collect { orders ->
                _pendingOrders.value = orders
                
                // Update current order if it exists in the list
                _currentOrder.value?.let { current ->
                    orders.find { it.id == current.id }?.let { updated ->
                        _currentOrder.value = updated
                    }
                }
            }
        }
    }

    fun placeOrder(order: OrderRequest, userId: String) {
        val finalOrder = order.copy(
            id = UUID.randomUUID().toString().substring(0, 8),
            customerId = userId
        )
        _currentOrder.value = finalOrder
        viewModelScope.launch {
            try {
                orderRepository.placeOrder(finalOrder)
                // Once placed, start observing this specific order for status updates
                observeOrderById(finalOrder.id)
            } catch (e: Exception) {
                // In a real app, handle error (e.g., save locally or show message)
                println("Error placing order: ${e.message}")
            }
        }
    }

    private fun observeOrderById(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrderById(orderId).collectLatest { updatedOrder ->
                if (updatedOrder != null) {
                    _currentOrder.value = updatedOrder
                }
            }
        }
    }

    fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        viewModelScope.launch {
            try {
                orderRepository.acceptOrder(orderId, driverId, driverName)
            } catch (e: Exception) {
                println("Error accepting order: ${e.message}")
            }
        }
    }

    fun updateDriverLocation(orderId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                orderRepository.updateDriverLocation(orderId, lat, lng)
            } catch (e: Exception) {
                println("Error updating location: ${e.message}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
            } catch (e: Exception) {
                println("Error updating status: ${e.message}")
            }
        }
    }
}
