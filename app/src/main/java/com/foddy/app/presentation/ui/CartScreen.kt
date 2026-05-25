package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.foddy.app.domain.model.CartItem
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.CartViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng của bạn") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            if (cartViewModel.cartItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tổng cộng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${cartViewModel.totalPrice.toInt()}đ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Screen.Checkout.route) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Thanh toán",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (cartViewModel.cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Giỏ hàng trống", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartViewModel.cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { cartViewModel.addToCart(item.foodItem) },
                        onDecrease = { cartViewModel.removeFromCart(item.foodItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(LightGray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.foodItem.name, fontWeight = FontWeight.Bold)
            Text(text = "${item.foodItem.price.toInt()}đ", color = Primary)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = LightGray)
            ) {
                Icon(
                    imageVector = if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold)
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
