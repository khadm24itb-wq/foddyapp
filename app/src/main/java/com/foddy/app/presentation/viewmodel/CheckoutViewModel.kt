package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.BankAccount
import com.foddy.app.domain.model.CartItem
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.PaymentMethod
import com.foddy.app.domain.model.PaymentStatus
import com.foddy.app.domain.repository.CartRepository
import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// 1. State: Đại diện cho toàn bộ dữ liệu trên màn hình Checkout
data class CheckoutUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val customerId: String = "",
    val customerName: String = "",
    val deliveryAddress: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val orderId: String = "",
    val bankAccount: BankAccount? = null,
    val error: String? = null
)

// 2. Effect: Xử lý các sự kiện một lần như Navigation hoặc Show Snackbar
sealed class CheckoutUiEffect {
    object NavigateToSuccess : CheckoutUiEffect()
    data class ShowError(val message: String) : CheckoutUiEffect()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CheckoutUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeCartAndUser()
        generateOrderDetails()
    }

    private fun observeCartAndUser() {
        // Observe Cart Items
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { items ->
                _uiState.update { it.copy(cartItems = items) }
            }
        }
        // Observe Total Price
        viewModelScope.launch {
            cartRepository.getTotalPrice().collectLatest { total ->
                _uiState.update { it.copy(totalPrice = total) }
            }
        }
        // Observe User Profile (Real-time từ Firestore/Room)
        viewModelScope.launch {
            userRepository.getCurrentUser().collectLatest { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            customerId = it.id,
                            customerName = it.name,
                            deliveryAddress = it.address
                        )
                    }
                }
            }
        }
    }

    private fun generateOrderDetails() {
        _uiState.update { 
            it.copy(
                orderId = "FD-${UUID.randomUUID().toString().take(6).uppercase()}",
                bankAccount = BankAccount(
                    bankName = "MB Bank",
                    bankCode = "MB",
                    accountNumber = "0345678999",
                    accountName = "FODDY APP SYSTEM"
                )
            )
        }
    }

    fun onAddressChanged(newAddress: String) {
        _uiState.update { it.copy(deliveryAddress = newAddress, error = null) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun simulatePaymentSuccess() {
        if (_uiState.value.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
            _uiState.update { it.copy(paymentStatus = PaymentStatus.PAID) }
        }
    }

    fun placeOrder() {
        val currentState = _uiState.value
        
        // 1. Validation (UX: Thông báo lỗi cụ thể)
        if (currentState.deliveryAddress.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập địa chỉ giao hàng") }
            return
        }

        if (currentState.cartItems.isEmpty()) {
            _uiState.update { it.copy(error = "Giỏ hàng của bạn đang trống") }
            return
        }

        viewModelScope.launch {
            // 2. Loading State (UX: Disable button, show progress)
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Tạo OrderRequest chuẩn từ dữ liệu hiện tại
                val order = OrderRequest(
                    id = currentState.orderId,
                    customerId = currentState.customerId,
                    restaurantName = "Foddy Store",
                    address = currentState.deliveryAddress,
                    totalAmount = currentState.totalPrice,
                    items = currentState.cartItems,
                    paymentMethod = when(currentState.selectedPaymentMethod) {
                        PaymentMethod.CASH_ON_DELIVERY -> "COD"
                        PaymentMethod.BANK_TRANSFER -> "VietQR"
                    },
                    status = "pending"
                )

                // 3. Thực hiện đặt hàng qua Repository (Firebase + Offline support)
                orderRepository.placeOrder(order)

                // 4. Gửi thông báo Notification
                notificationRepository.sendOrderStatusNotification(
                    userId = currentState.customerName,
                    orderId = currentState.orderId,
                    event = OrderEvent.OrderConfirmed
                )

                // 5. Xóa giỏ hàng (Xử lý Offline-first thông qua Room)
                cartRepository.clearCart()

                // 6. Thành công: Chuyển trạng thái và điều hướng
                _uiState.update { it.copy(isLoading = false, paymentStatus = PaymentStatus.PAID) }
                _uiEffect.send(CheckoutUiEffect.NavigateToSuccess)

            } catch (e: Exception) {
                // 7. Error Handling chuyên nghiệp
                _uiState.update { it.copy(isLoading = false, error = "Đặt hàng thất bại: ${e.message}") }
                _uiEffect.send(CheckoutUiEffect.ShowError(e.message ?: "Lỗi hệ thống, vui lòng thử lại"))
            }
        }
    }
}
