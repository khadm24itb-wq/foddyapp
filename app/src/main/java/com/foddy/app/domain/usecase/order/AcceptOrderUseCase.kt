package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.repository.OrderRepository
import javax.inject.Inject

class AcceptOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, driverId: String, driverName: String) {
        repository.acceptOrder(orderId, driverId, driverName)
    }
}
