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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.presentation.viewmodel.MenuViewModel
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantAdminScreen(navController: NavController, menuViewModel: MenuViewModel) {
    val menuItems by menuViewModel.foodItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Form states
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
                            imageRes = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
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

    Scaffold(
        topBar = { 
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Quản lý Thực đơn", fontWeight = FontWeight.Bold)
                        Text("Cửa hàng của bạn", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.White)
            ) 
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm món") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Stats Row
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
                model = item.imageRes,
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

