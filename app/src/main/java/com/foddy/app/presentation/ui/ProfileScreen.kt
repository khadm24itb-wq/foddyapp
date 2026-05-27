package com.foddy.app.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.presentation.ui.theme.LightGray
import com.foddy.app.presentation.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel) {
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(userProfile.name) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Chỉnh sửa hồ sơ") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Họ và tên") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.updateProfile(editName)
                    showEditDialog = false
                }) {
                    Text("Lưu")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hồ sơ của tôi") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            AsyncImage(
                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userProfile.name.ifEmpty { "Khách" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = userProfile.email.ifEmpty { "Chưa có email" }, fontSize = 14.sp, color = Color.Gray)
            
            Button(
                onClick = { 
                    editName = userProfile.name
                    showEditDialog = true 
                },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Color.Black)
            ) {
                Text("Chỉnh sửa thông tin")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Favorite, title = "Món yêu thích") { }
            ProfileMenuItem(icon = Icons.Default.History, title = "Lịch sử đơn hàng") {
                navController.navigate(Screen.OrdersHistory.route)
            }
            ProfileMenuItem(icon = Icons.Default.LocationOn, title = "Địa chỉ của tôi") { }
            ProfileMenuItem(icon = Icons.Default.Payment, title = "Phương thức thanh toán") { }
            ProfileMenuItem(icon = Icons.Default.Settings, title = "Cài đặt") { }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(
                onClick = {
                    userViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    HorizontalDivider(color = LightGray)
}
