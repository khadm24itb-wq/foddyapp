package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(navController: NavController, orderViewModel: OrderViewModel) {
    val orders by orderViewModel.userOrders.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Assume we have a way to get current user ID, for now using demo
        orderViewModel.listenToUserOrders("user_001")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lịch sử đơn hàng") })
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Bạn chưa có đơn hàng nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryCard(order)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: OrderRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${order.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Đơn hàng: #${order.id.takeLast(6)}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(order.status) {
                        "pending" -> "Đang chờ"
                        "preparing" -> "Đang chuẩn bị"
                        "delivering" -> "Đang giao"
                        "completed" -> "Hoàn thành"
                        else -> order.status
                    },
                    color = when(order.status) {
                        "completed" -> Color(0xFF4CAF50)
                        "delivering", "preparing" -> Color(0xFF2196F3)
                        "pending" -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = { /* TODO: Đặt lại */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Đặt lại", fontSize = 12.sp)
                }
            }
        }
    }
}
