package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.data.DummyData
import com.foddy.app.domain.model.FoodItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.foddy.app.presentation.viewmodel.CartViewModel
import com.foddy.app.presentation.viewmodel.MenuViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    navController: NavController,
    restaurantId: String?,
    cartViewModel: CartViewModel,
    menuViewModel: MenuViewModel
) {
    val menuItems by menuViewModel.foodItems.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    
    val restaurant = DummyData.restaurants.find { it.id == restaurantId } ?: return
    // Sử dụng menu từ ViewModel nếu là nhà hàng chính (id=1), còn lại dùng dummy
    val displayMenu = if (restaurant.id == "1") menuItems else restaurant.menu

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = restaurant.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { navController.navigate("cart") },
                    color = Primary,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cartItems.sumOf { it.quantity }} món",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Xem giỏ hàng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${totalPrice.toInt()}đ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                AsyncImage(
                    model = restaurant.imageRes,
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = restaurant.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Primary)
                        Text(text = restaurant.rating.toString(), fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = restaurant.description, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Thực đơn", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(displayMenu) { foodItem ->
                FoodItemCard(foodItem) {
                    cartViewModel.addToCart(foodItem)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FoodItemCard(foodItem: FoodItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = foodItem.imageRes,
                contentDescription = foodItem.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = foodItem.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = foodItem.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                Text(text = "${foodItem.price.toInt()}đ", fontSize = 16.sp, color = Primary, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = onAddClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}
