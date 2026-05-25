package com.foddy.app.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foddy.app.domain.model.PaymentMethod
import com.foddy.app.domain.model.PaymentStatus
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.Primary
import com.foddy.app.presentation.viewmodel.CartViewModel
import com.foddy.app.presentation.viewmodel.CheckoutViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.util.QRGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    userViewModel: UserViewModel,
    checkoutViewModel: CheckoutViewModel = hiltViewModel()
) {
    val address by remember { mutableStateOf("123 Đường Cầu Giấy, Hà Nội") }
    val userProfile by userViewModel.user.collectAsState()
    val checkoutUiState by checkoutViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Đồng bộ tổng tiền từ Cart
    LaunchedEffect(cartViewModel.totalPrice) {
        checkoutViewModel.setTotal(cartViewModel.totalPrice.toLong())
    }

    // Hiển thị thông báo lỗi nếu có
    LaunchedEffect(checkoutUiState.error) {
        checkoutUiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (checkoutUiState.paymentStatus == PaymentStatus.PAID) {
        PaymentSuccessContent(
            amount = checkoutUiState.total,
            onDone = {
                cartViewModel.clearCart()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .imePadding()
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            SectionTitle("Địa chỉ nhận hàng")
                            AddressCard(address)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SectionTitle("Phương thức thanh toán")
                            PaymentMethodsSection(checkoutViewModel, checkoutUiState)
                            
                            // Hiển thị mã VietQR nếu chọn chuyển khoản
                            AnimatedVisibility(visible = checkoutUiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
                                BankQRCard(
                                    account = checkoutUiState.bankAccount,
                                    amount = checkoutUiState.total,
                                    orderId = checkoutUiState.orderId
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SectionTitle("Tóm tắt đơn hàng")
                            OrderSummaryCard(cartViewModel)
                        }
                    }

                    Button(
                        onClick = {
                            checkoutViewModel.processPayment(
                                customerName = userProfile.name.ifBlank { "Khách hàng" },
                                items = cartViewModel.cartItems.map { it.foodItem.name },
                                address = address
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !checkoutUiState.isLoading
                    ) {
                        if (checkoutUiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            val btnText = if (checkoutUiState.selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) 
                                "Đặt hàng ngay (COD)" else "Xác nhận đã chuyển khoản"
                            Text(btnText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Loading Overlay khi đang xử lý
                if (checkoutUiState.isLoading) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Đang xử lý đơn hàng...", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun AddressCard(address: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(address, modifier = Modifier.weight(1f), fontSize = 14.sp)
        }
    }
}

@Composable
fun PaymentMethodsSection(viewModel: CheckoutViewModel, uiState: com.foddy.app.presentation.viewmodel.CheckoutUiState) {
    Column {
        PaymentOption(
            title = "Tiền mặt khi nhận hàng (COD)",
            icon = "💵",
            isSelected = uiState.selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
            onSelect = { viewModel.selectPaymentMethod(PaymentMethod.CASH_ON_DELIVERY) }
        )
        PaymentOption(
            title = "Chuyển khoản VietQR",
            icon = "🏦",
            isSelected = uiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER,
            onSelect = { viewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER) }
        )
    }
}

@Composable
fun PaymentOption(title: String, icon: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onSelect() }
            .padding(8.dp)
    ) {
        RadioButton(selected = isSelected, onClick = onSelect, colors = RadioButtonDefaults.colors(selectedColor = Primary))
        Text(icon, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
        Text(title, fontSize = 15.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BankQRCard(account: com.foddy.app.domain.model.BankAccount?, amount: Long, orderId: String) {
    Card(
        modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Quét mã để thanh toán", fontWeight = FontWeight.Bold, color = Color.DarkGray)
            
            val qrBitmap = remember(orderId) { 
                account?.let { QRGenerator.generateVietQRBitmap(it, orderId, amount) } 
            }
            
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR",
                    modifier = Modifier.size(200.dp).padding(8.dp).clip(RoundedCornerShape(8.dp))
                )
            }

            Text("Nội dung: FODDY$orderId", fontWeight = FontWeight.ExtraBold, color = Primary)
            Text("Số tiền: ${amount.formatVND()}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderSummaryCard(cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            cartViewModel.cartItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("${item.quantity}x ${item.foodItem.name}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text("${(item.foodItem.price * item.quantity).toInt()}đ", fontSize = 14.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${cartViewModel.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold, color = Primary, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PaymentSuccessContent(amount: Long, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(100.dp), tint = Primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Đặt hàng thành công!", fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
        Text("Tổng thanh toán: ${amount.formatVND()}", modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onDone, modifier = Modifier.padding(top = 32.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Về trang chủ")
        }
    }
}

private fun Long.formatVND(): String = 
    java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(this) + " đ"
