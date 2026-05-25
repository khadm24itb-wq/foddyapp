package com.foddy.app.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAppScreen(navController: NavController, orderViewModel: OrderViewModel) {
    var isOnline by remember { mutableStateOf(false) }
    val allOrders by orderViewModel.pendingOrders.collectAsStateWithLifecycle()
    val currentDriverId = "1" 

    val myOrders by remember(allOrders, isOnline) {
        derivedStateOf {
            if (!isOnline) emptyList()
            else allOrders.filter { 
                it.status == "pending" || 
                (it.driverId == currentDriverId && (it.status == "accepted" || it.status == "delivering"))
            }
        }
    }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { 
                    Text("Trung tâm Tài xế", fontWeight = FontWeight.ExtraBold) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Switch(
                            checked = isOnline, 
                            onCheckedChange = { isOnline = it }, 
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Status & Stats Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        if (isOnline) Brush.verticalGradient(listOf(Color.White, Color(0xFFE8F5E9)))
                        else Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F5F5)))
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DriverStatItem(Icons.Default.Payments, "Thu nhập", "450k", Primary)
                        DriverStatItem(Icons.Default.DirectionsBike, "Số đơn", "12", Color(0xFF2196F3))
                        DriverStatItem(Icons.Default.Timer, "Trực tuyến", "5h 20p", Color(0xFFFF9800))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Surface(
                        shape = CircleShape,
                        color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isOnline) "● ĐANG TRỰC TUYẾN" else "○ ĐANG NGOẠI TUYẾN",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = if (isOnline) Color(0xFF2E7D32) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Đơn hàng khả dụng (${myOrders.size})", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                if (!isOnline) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DirectionsBike, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Text("Bật trực tuyến để nhận đơn ngay!", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                } else if (myOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hiện chưa có đơn hàng nào...", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp), 
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(
                            items = myOrders,
                            key = { it.id }
                        ) { order ->
                            val statusLabel = when(order.status) {
                                "pending" -> "ĐƠN MỚI"
                                "accepted" -> "BẠN ĐÃ NHẬN"
                                "delivering" -> "ĐANG GIAO"
                                else -> order.status
                            }
                            
                            OrderRequestCard(
                                orderId = "#${order.id.takeLast(6)}",
                                restaurant = order.restaurantName,
                                address = order.address,
                                price = "${order.totalAmount.toInt()}đ",
                                status = statusLabel,
                                isSimulating = order.status == "delivering",
                                onSimulateToggle = {
                                    if (order.status == "delivering") {
                                        orderViewModel.startLocationSimulation(order.id)
                                    }
                                },
                                buttonText = when(order.status) {
                                    "pending" -> "Nhận đơn ngay"
                                    "accepted" -> "Bắt đầu giao"
                                    "delivering" -> "Hoàn thành"
                                    else -> "Đã xong"
                                }
                            ) {
                                when(order.status) {
                                    "pending" -> orderViewModel.acceptOrder(order.id, currentDriverId, "Trần Văn Gấu")
                                    "accepted" -> orderViewModel.updateOrderStatus(order.id, "delivering")
                                    "delivering" -> {
                                        orderViewModel.stopLocationSimulation()
                                        orderViewModel.updateOrderStatus(order.id, "completed")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverStatItem(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF333333))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun OrderRequestCard(
    orderId: String, 
    restaurant: String, 
    address: String, 
    price: String, 
    status: String,
    buttonText: String,
    isSimulating: Boolean = false,
    onSimulateToggle: () -> Unit = {},
    onAccept: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (status.contains("KHÁCH")) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status, 
                        color = if (status.contains("KHÁCH")) Color.Red else Color(0xFF1976D2), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(text = price, color = Primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = restaurant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp).padding(top = 2.dp), 
                    tint = Color.Gray
                )
                Text(
                    text = address, 
                    fontSize = 14.sp, 
                    color = Color.DarkGray, 
                    modifier = Modifier.padding(start = 6.dp),
                    lineHeight = 20.sp
                )
            }
            
            if (status == "ĐANG GIAO") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mô phỏng di chuyển", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isSimulating,
                        onCheckedChange = { onSimulateToggle() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status.contains("KHÁCH")) Color(0xFFE64A19) else Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

