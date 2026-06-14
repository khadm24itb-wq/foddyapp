package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.presentation.viewmodel.OrderViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAppScreen(
    navController: NavController, 
    orderViewModel: OrderViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    var isOnline by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        orderViewModel.listenToPendingOrders()
    }

    val allOrders by orderViewModel.pendingOrders.collectAsStateWithLifecycle()
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val chatMessages by orderViewModel.chatMessages.collectAsStateWithLifecycle()
    val currentDriverId = user.id

    var showChatDialog by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf("") }
    var chatMessageText by remember { mutableStateOf("") }
    var targetUserId by remember { mutableStateOf("") }

    val filteredOrders by remember(allOrders, isOnline, currentDriverId, selectedTab) {
        derivedStateOf {
            if (!isOnline && selectedTab != 2) emptyList()
            else {
                when (selectedTab) {
                    0 -> allOrders.filter { 
                        (it.status == "PENDING" || it.status == "CONFIRMED" || it.status == "PREPARING") && 
                        it.driverId.isNullOrEmpty() 
                    }
                    1 -> allOrders.filter { 
                        (it.status == "PREPARING" || it.status == "DELIVERING") && 
                        !it.driverId.isNullOrEmpty() && it.driverId == currentDriverId
                    }
                    2 -> allOrders.filter { it.status == "COMPLETED" && it.driverId == currentDriverId }
                    else -> emptyList()
                }
            }
        }
    }

    if (showChatDialog) {
        Dialog(
            onDismissRequest = { showChatDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chat với khách hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showChatDialog = false }) {
                            Text("Đóng", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                    val listState = rememberLazyListState()
                    LaunchedEffect(chatMessages.size) {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(chatMessages) { message ->
                                val isMe = message.senderId == currentDriverId
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Surface(
                                        color = if (isMe) Primary else Color(0xFFF1F3F4),
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp, 
                                            topEnd = 16.dp, 
                                            bottomStart = if (isMe) 16.dp else 2.dp, 
                                            bottomEnd = if (isMe) 2.dp else 16.dp
                                        ),
                                        modifier = Modifier.widthIn(max = 260.dp)
                                    ) {
                                        Text(
                                            text = message.message,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            color = if (isMe) Color.White else Color.Black,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("Nhập tin nhắn...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (chatMessageText.isNotBlank() && selectedOrderId.isNotEmpty()) {
                                    orderViewModel.sendChatMessage(
                                        message = chatMessageText,
                                        senderId = currentDriverId,
                                        receiverId = targetUserId,
                                        orderId = selectedOrderId
                                    )
                                    chatMessageText = ""
                                }
                            },
                            containerColor = Primary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Gửi", modifier = Modifier.size(20.dp))
                        }
                    }
                }
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
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { 
                            orderViewModel.listenToPendingOrders()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Primary)
                        }
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Tìm đơn", modifier = Modifier.padding(16.dp), fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Đang giao", modifier = Modifier.padding(16.dp), fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Lịch sử", modifier = Modifier.padding(16.dp), fontWeight = if(selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                }
            }

            if (selectedTab != 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (isOnline) "● ĐANG TRỰC TUYẾN" else "○ ĐANG NGOẠI TUYẾN",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = if (isOnline) Color(0xFF2E7D32) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (!isOnline && selectedTab != 2) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Text("Bật trực tuyến để nhận đơn!", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                } else if (filteredOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(when(selectedTab) {
                            0 -> "Hiện chưa có đơn hàng mới nào..."
                            1 -> "Bạn chưa nhận đơn hàng nào."
                            else -> "Lịch sử đơn hàng trống."
                        }, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp), 
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(
                            items = filteredOrders,
                            key = { it.id }
                        ) { order ->
                            val statusLabel = when(order.status) {
                                "CONFIRMED" -> "ĐƠN MỚI"
                                "PREPARING" -> "QUÁN ĐANG LÀM"
                                "DELIVERING" -> "ĐANG GIAO"
                                "COMPLETED" -> "ĐÃ GIAO XONG"
                                else -> order.status
                            }
                            
                            val isAcceptedByMe = !order.driverId.isNullOrEmpty() && order.driverId == currentDriverId
                            val canAccept = order.driverId.isNullOrEmpty()

                            OrderRequestCard(
                                orderId = "#${if (order.id.length > 6) order.id.takeLast(6) else order.id}",
                                restaurant = order.restaurantName,
                                address = order.address,
                                price = "${order.totalPrice.toInt()}đ",
                                status = statusLabel,
                                isSimulating = order.status == "DELIVERING",
                                onSimulateToggle = {
                                    if (order.status == "DELIVERING") {
                                        orderViewModel.startLocationSimulation(order.id)
                                    }
                                },
                                buttonText = when {
                                    canAccept -> "Nhận đơn"
                                    order.status == "PREPARING" -> "Đã lấy hàng"
                                    order.status == "DELIVERING" -> "Hoàn thành"
                                    order.status == "COMPLETED" -> "Nhắn tin"
                                    else -> "Chi tiết"
                                },
                                onChatClick = if (isAcceptedByMe) {
                                    {
                                        selectedOrderId = order.id
                                        targetUserId = order.userId
                                        orderViewModel.trackOrder(order.id)
                                        showChatDialog = true
                                    }
                                } else null
                            ) {
                                when {
                                    canAccept -> {
                                        if (currentDriverId.isNotEmpty()) {
                                            orderViewModel.acceptOrder(order.id, currentDriverId, user.name)
                                        }
                                    }
                                    isAcceptedByMe && order.status == "PREPARING" -> {
                                        orderViewModel.updateOrderStatus(order.id, "DELIVERING")
                                    }
                                    isAcceptedByMe && order.status == "DELIVERING" -> {
                                        orderViewModel.stopLocationSimulation()
                                        orderViewModel.updateOrderStatus(order.id, "COMPLETED")
                                    }
                                    order.status == "COMPLETED" -> {
                                        selectedOrderId = order.id
                                        targetUserId = order.userId
                                        orderViewModel.trackOrder(order.id)
                                        showChatDialog = true 
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
    onChatClick: (() -> Unit)? = null,
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
                    color = if (status.contains("ĐƠN MỚI")) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status, 
                        color = if (status.contains("ĐƠN MỚI")) Color.Red else Color(0xFF1976D2), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onChatClick != null) {
                        IconButton(
                            onClick = onChatClick,
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(text = price, color = Primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
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
                    containerColor = if (status.contains("ĐƠN MỚI")) Color(0xFFE64A19) else Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
