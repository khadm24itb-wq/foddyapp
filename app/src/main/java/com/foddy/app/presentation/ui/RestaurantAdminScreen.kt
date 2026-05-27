package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.presentation.viewmodel.AIViewModel
import com.foddy.app.presentation.viewmodel.MenuViewModel
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantAdminScreen(
    navController: NavController, 
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Đơn hàng", "Thực đơn", "Phân tích AI")

    Scaffold(
        topBar = { 
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("Quản lý Nhà hàng", fontWeight = FontWeight.ExtraBold) 
                    },
                    actions = {
                        IconButton(onClick = { userViewModel.logout(); navController.navigate("login") { popUpTo(0) } }) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                0 -> OrderManagementSection(orderViewModel)
                1 -> MenuManagementSection(menuViewModel)
                2 -> AIAnalyticsSection(orderViewModel)
            }
        }
    }
}

@Composable
fun AIAnalyticsSection(orderViewModel: OrderViewModel) {
    val aiViewModel: AIViewModel = hiltViewModel()
    val orders by orderViewModel.restaurantOrders.collectAsStateWithLifecycle()
    val insights by aiViewModel.businessInsights.collectAsStateWithLifecycle()
    val isAnalyzing by aiViewModel.isAnalyzingBusiness.collectAsStateWithLifecycle()

    LaunchedEffect(orders) {
        if (orders.isNotEmpty() && insights == null) {
            aiViewModel.getBusinessInsights(orders)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Phân tích thông minh", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isAnalyzing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                    Text("Đang phân tích dữ liệu kinh doanh...", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text(
                        text = insights ?: "Chưa có dữ liệu phân tích. Hãy nhận thêm đơn hàng để AI có thể giúp bạn.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Revenue Chart
        Card(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Doanh thu 7 ngày qua (VNĐ)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedRevenueChart()
            }
        }

        // Recommendations
        Text("Khuyến nghị từ AI", fontWeight = FontWeight.Bold)
        
        AIInsightItem(
            icon = Icons.Default.TrendingUp,
            title = "Tăng cường Flash Sale",
            desc = "Giảm giá 10% cho Pizza vào khung giờ 14h-16h để tối ưu doanh thu thấp điểm."
        )
        
        AIInsightItem(
            icon = Icons.Default.Inventory,
            title = "Quản lý kho",
            desc = "Nhu cầu Gà rán dự kiến tăng cao vào tối Thứ 6. Hãy chuẩn bị thêm nguyên liệu."
        )
    }
}

@Composable
fun AnimatedRevenueChart() {
    val revenueData = listOf(1200000f, 1500000f, 1100000f, 1800000f, 2200000f, 1900000f, 2500000f)
    val maxRevenue = revenueData.maxOrNull() ?: 1f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        revenueData.forEachIndexed { index, value ->
            val barHeight = (value / maxRevenue) * 120
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Primary, Primary.copy(alpha = 0.5f))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "T${index + 2}", 
                    fontSize = 10.sp, 
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun AIInsightItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun OrderManagementSection(orderViewModel: OrderViewModel) {
    val rawOrders by orderViewModel.restaurantOrders.collectAsStateWithLifecycle()
    val restaurantId = "rest_001" // Demo ID

    // Tối ưu hóa: Chỉ sắp xếp lại khi rawOrders thay đổi
    val sortedOrders by remember(rawOrders) {
        derivedStateOf {
            rawOrders.sortedByDescending { it.timestamps["placedAt"] ?: 0L }
        }
    }

    LaunchedEffect(Unit) {
        orderViewModel.listenToRestaurantOrders(restaurantId)
    }

    if (sortedOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có đơn hàng nào", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = sortedOrders,
                key = { it.id } // Thêm key để LazyColumn ổn định hơn
            ) { order ->
                RestaurantOrderCard(order) { newStatus ->
                    orderViewModel.updateOrderStatus(order.id, newStatus)
                }
            }
        }
    }
}

@Composable
fun RestaurantOrderCard(order: OrderRequest, onStatusUpdate: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Đơn #${order.id.takeLast(6)}", fontWeight = FontWeight.Bold, color = Color.Gray)
                Surface(
                    color = when(order.status) {
                        "pending" -> Color(0xFFFFF3E0)
                        "preparing" -> Color(0xFFE3F2FD)
                        "delivering" -> Color(0xFFE8F5E9)
                        "completed" -> Color(0xFFF5F5F5)
                        else -> Color(0xFFF5F5F5)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when(order.status) {
                            "pending" -> "MỚI"
                            "preparing" -> "ĐANG CHẾ BIẾN"
                            "delivering" -> "ĐANG GIAO"
                            "completed" -> "HOÀN THÀNH"
                            else -> order.status.uppercase()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when(order.status) {
                            "pending" -> Color(0xFFEF6C00)
                            "preparing" -> Color(0xFF1976D2)
                            "delivering" -> Color(0xFF2E7D32)
                            "completed" -> Color.Gray
                            else -> Color.Gray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}x ${item.foodItem.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${(item.foodItem.price * item.quantity).toInt()}đ", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                    Text("${order.totalPrice.toInt()}đ", fontWeight = FontWeight.ExtraBold, color = Primary, fontSize = 18.sp)
                }
                
                if (order.status == "pending") {
                    Button(
                        onClick = { onStatusUpdate("preparing") },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nhận đơn", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == "preparing") {
                    OutlinedButton(
                        onClick = { /* Wait for driver */ },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Chờ tài xế...", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuManagementSection(menuViewModel: MenuViewModel) {
    val menuItems by menuViewModel.foodItems.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isFlashSale by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm món ăn mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Tên món") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = { Text("Giá tiền (đ)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Surface(
                        color = Color.Gray.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Checkbox(checked = isFlashSale, onCheckedChange = { isFlashSale = it })
                            Text("Kích hoạt Flash Sale (Giảm 20%)", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newItem = FoodItem(
                            id = (menuItems.size + 1).toString(),
                            name = name,
                            description = "Món ăn mới thêm",
                            price = price.toDoubleOrNull() ?: 0.0,
                            discountPrice = if (isFlashSale) (price.toDoubleOrNull() ?: 0.0) * 0.8 else null,
                            image = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                            rating = 5.0,
                            calories = 300,
                            isFlashSale = isFlashSale
                        )
                        menuViewModel.addFoodItem(newItem)
                        showAddDialog = false
                        name = ""; price = ""; isFlashSale = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Lưu món") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Hủy") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(Modifier.weight(1f), "Tổng món", menuItems.size.toString(), Color(0xFF2196F3))
                AdminStatCard(Modifier.weight(1f), "Đang giảm giá", menuItems.count { it.isFlashSale }.toString(), Color(0xFFF44336))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems) { item ->
                    AdminFoodCard(item) { menuViewModel.removeFoodItem(item) }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminFoodCard(item: FoodItem, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${item.price.toInt()}đ", color = Primary, fontWeight = FontWeight.Bold)
                    if (item.isFlashSale) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Flash Sale", 
                                color = Color.Red, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text("Mô tả: ${item.description}", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.clip(CircleShape).background(Color(0xFFFFEBEE))
            ) { 
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp)) 
            }
        }
    }
}
