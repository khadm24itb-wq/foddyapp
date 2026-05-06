package com.example.foodle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodle.navigation.Screen
import com.example.foodle.ui.CartViewModel
import com.example.foodle.ui.OrderRequest
import com.example.foodle.ui.OrderViewModel
import com.example.foodle.ui.theme.Primary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, cartViewModel: CartViewModel, orderViewModel: OrderViewModel, userViewModel: com.example.foodle.ui.UserViewModel) {
    var address by remember { mutableStateOf("123 Đường Cầu Giấy, Hà Nội") }
    val userProfile by userViewModel.user.collectAsState()
    var paymentMethod by remember { mutableStateOf("Tiền mặt") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Address Section
            Text("Địa chỉ nhận hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(address, modifier = Modifier.weight(1f))
                    TextButton(onClick = { /* Edit address */ }) { Text("Thay đổi") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Summary
            Text("Tóm tắt đơn hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                items(cartViewModel.cartItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity}x ${item.foodItem.name}")
                        Text("${(item.foodItem.price * item.quantity).toInt()}đ")
                    }
                }
            }

            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${cartViewModel.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Primary)
            }

            Button(
                onClick = {
                    val newOrder = OrderRequest(
                        restaurantName = "Hà Nội Quán",
                        address = address,
                        totalAmount = cartViewModel.totalPrice,
                        items = cartViewModel.cartItems.toList()
                    )
                    orderViewModel.placeOrder(newOrder, userProfile.email) // Sử dụng email làm UID demo
                    
                    cartViewModel.clearCart()
                    navController.navigate(Screen.DriverSelection.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Đặt hàng ngay", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
