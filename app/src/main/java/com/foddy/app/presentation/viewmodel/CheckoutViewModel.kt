package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.BankAccount
import com.foddy.app.domain.model.PaymentMethod
import com.foddy.app.domain.model.PaymentStatus
import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.repository.OrderInfo
import com.foddy.app.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val total: Long = 0,
    val restaurantName: String = "",
    val restaurantId: String = "rest_123",
    val orderId: String = "",
    val bankAccount: BankAccount? = null,
    val error: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { 
            it.copy(
                orderId = UUID.randomUUID().toString().take(8).uppercase(),
                total = 150000,
                restaurantName = "Phở Thìn Lò Đúc",
                bankAccount = BankAccount(
                    bankName = "Vietcombank",
                    bankCode = "VCB",
                    accountNumber = "1234567890",
                    accountName = "NGUYEN VAN A"
                )
            )
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setTotal(amount: Long) {
        _uiState.update { it.copy(total = amount) }
    }

    fun processPayment(customerName: String, items: List<String>, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Giả lập xử lý đặt hàng
            delay(1500)
            
            val orderInfo = OrderInfo(
                orderId = _uiState.value.orderId,
                customerName = customerName,
                items = items,
                totalAmount = _uiState.value.total,
                deliveryAddress = address,
                paymentMethod = _uiState.value.selectedPaymentMethod.name
            )

            // Gửi thông báo đơn hàng mới cho chủ quán
            notificationRepository.sendNewOrderNotification(
                restaurantId = _uiState.value.restaurantId,
                orderId = _uiState.value.orderId,
                orderInfo = orderInfo
            )

            if (_uiState.value.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
                // Gửi thông báo xác nhận thanh toán (giả lập khách đã chuyển)
                notificationRepository.sendPaymentConfirmedNotification(
                    restaurantId = _uiState.value.restaurantId,
                    orderId = _uiState.value.orderId,
                    amount = _uiState.value.total
                )
            }

            // Thông báo cho khách hàng
            notificationRepository.sendOrderStatusNotification(
                userId = "current_user",
                orderId = _uiState.value.orderId,
                event = OrderEvent.OrderConfirmed
            )

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    paymentStatus = PaymentStatus.PAID 
                ) 
            }
        }
    }

    fun resetState() {
        _uiState.update { 
            CheckoutUiState(
                orderId = UUID.randomUUID().toString().take(8).uppercase(),
                total = 150000, // Default total
                restaurantName = "Phở Thìn Lò Đúc",
                bankAccount = BankAccount(
                    bankName = "Vietcombank",
                    bankCode = "VCB",
                    accountNumber = "1234567890",
                    accountName = "NGUYEN VAN A"
                )
            ) 
        }
    }
}
