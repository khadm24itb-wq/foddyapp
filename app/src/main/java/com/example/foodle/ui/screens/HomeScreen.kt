package com.example.foodle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.foodle.data.DummyData
import com.example.foodle.model.FoodItem
import com.example.foodle.model.Restaurant
import com.example.foodle.navigation.Screen
import com.example.foodle.ui.MenuViewModel
import com.example.foodle.ui.RecommendationViewModel
import com.example.foodle.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, 
    menuViewModel: MenuViewModel,
    recommendationViewModel: RecommendationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    restaurantViewModel: com.example.foodle.ui.RestaurantViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val menuItems by menuViewModel.foodItems.collectAsState()
    val aiRecommendations by recommendationViewModel.recommendations.collectAsState()
    val isLoadingAi by recommendationViewModel.isLoading.collectAsState()
    
    val restaurants by restaurantViewModel.restaurants.collectAsState()
    val isLoadingRestaurants by restaurantViewModel.isLoading.collectAsState()
    
    val flashSaleItems = menuItems.filter { it.isFlashSale }
    val scrollState = rememberScrollState()

    // Kiểm tra khi cuộn đến cuối để load thêm
    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0 && scrollState.value == scrollState.maxValue) {
            restaurantViewModel.loadMoreRestaurants()
        }
    }

    // Gọi AI gợi ý dựa trên sở thích mặc định hoặc lịch sử (giả lập)
    LaunchedEffect(menuItems) {
        if (menuItems.isNotEmpty()) {
            recommendationViewModel.getAIRecommendations("Tôi thích món ăn Việt Nam truyền thống và đồ uống thanh mát", menuItems)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Giao hàng đến", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Hà Nội, Việt Nam", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm kiếm món ăn...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI Recommendations Section
        if (isLoadingAi) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
            Text("AI đang tìm món ngon cho bạn...", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        } else if (aiRecommendations.isNotEmpty()) {
            Text(text = "Gợi ý từ AI ✨", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
            Text(text = "Dựa trên sở thích của bạn", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(aiRecommendations) { item ->
                    FlashSaleCard(item)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Flash Sale Section
        if (flashSaleItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Flash Sale ⚡", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Text(text = "Xem tất cả", fontSize = 14.sp, color = Primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(flashSaleItems) { item ->
                    FlashSaleCard(item)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Restaurants Section
        Text(text = "Quán ăn gần bạn", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        restaurants.forEach { restaurant ->
            RestaurantCard(restaurant) {
                navController.navigate(Screen.RestaurantDetail.createRoute(restaurant.id))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoadingRestaurants) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
fun FlashSaleCard(item: FoodItem) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageRes,
                contentDescription = null,
                modifier = Modifier.height(100.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${item.discountPrice?.toInt()}đ", color = Primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.price.toInt()}đ", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = restaurant.imageRes,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = restaurant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = restaurant.category, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Text(text = "${restaurant.rating} • ${restaurant.deliveryTime}", fontSize = 12.sp)
                }
            }
        }
    }
}
