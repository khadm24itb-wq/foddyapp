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
import com.foddy.app.presentation.viewmodel.CheckoutUiEffect
import com.foddy.app.presentation.viewmodel.CheckoutViewModel
import com.foddy.app.presentation.viewmodel.UserViewModel
import com.foddy.app.util.QRGenerator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    userViewModel: UserViewModel,
    checkoutViewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by checkoutViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Xử lý UiEffect (Navigation, Error)
    LaunchedEffect(Unit) {
        checkoutViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToSuccess -> {
                    // Thành công thì không cần clear cart thủ công ở đây vì ViewModel đã làm
                    // Nhưng có thể thêm logic tracking tại đây
                }
                is CheckoutUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    if (uiState.paymentStatus == PaymentStatus.PAID) {
        PaymentSuccessContent(
            amount = uiState.totalPrice.toLong(),
            orderId = uiState.orderId,
            onDone = {
                navController.navigate(Screen.OrderTracking.createRoute(uiState.orderId)) {
                    popUpTo(Screen.Home.route) { inclusive = false }
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
                            // UX: Cho phép người dùng sửa địa chỉ trực tiếp
                            OutlinedTextField(
                                value = uiState.deliveryAddress,
                                onValueChange = { checkoutViewModel.onAddressChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Nhập địa chỉ giao hàng") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Primary) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SectionTitle("Phương thức thanh toán")
                            PaymentMethodsSection(checkoutViewModel, uiState)
                            
                            // Hiển thị mã VietQR nếu chọn chuyển khoản
                            AnimatedVisibility(visible = uiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
                                BankQRCard(
                                    account = uiState.bankAccount,
                                    amount = uiState.totalPrice.toLong(),
                                    orderId = uiState.orderId,
                                    onSimulateSuccess = { checkoutViewModel.simulatePaymentSuccess() }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SectionTitle("Tóm tắt đơn hàng")
                            OrderSummaryCard(uiState.cartItems, uiState.totalPrice)
                        }
                    }

                    // Nút Đặt hàng với UX Loading & Disable
                    Button(
                        onClick = { checkoutViewModel.placeOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            val btnText = if (uiState.selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) 
                                "Đặt đơn hàng ngay" else "Tôi đã chuyển khoản thành công"
                            Text(btnText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Loading Overlay toàn màn hình (UX: Ngăn tương tác khi đang xử lý quan trọng)
                if (uiState.isLoading) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Đang xử lý đơn hàng...", fontWeight = FontWeight.Medium)
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
fun PaymentMethodsSection(viewModel: CheckoutViewModel, uiState: com.foddy.app.presentation.viewmodel.CheckoutUiState) {
    Column {
        PaymentOption(
            title = "Tiền mặt khi nhận hàng (COD)",
            icon = "💵",
            isSelected = uiState.selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
            onSelect = { viewModel.selectPaymentMethod(PaymentMethod.CASH_ON_DELIVERY) }
        )
        PaymentOption(
            title = "Chuyển khoản VietQR (Nhanh chóng)",
            icon = "🏦",
            isSelected = uiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER,
            onSelect = { viewModel.selectPaymentMethod(PaymentMethod.BANK_TRANSFER) }
        )
    }
}

@Composable
fun OrderSummaryCard(items: List<com.foddy.app.domain.model.CartItem>, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("${item.quantity}x ${item.foodItem.name}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text("${(item.foodItem.price * item.quantity).toInt()}đ", fontSize = 14.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Tổng thanh toán", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${total.toInt()}đ", fontWeight = FontWeight.Bold, color = Primary, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun BankQRCard(
    account: com.foddy.app.domain.model.BankAccount?,
    amount: Long,
    orderId: String,
    onSimulateSuccess: () -> Unit = {}
) {
    Card(
        modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Quét mã VietQR để thanh toán", fontWeight = FontWeight.Bold, color = Color.DarkGray)
            
            val qrBitmap = remember(orderId) { 
                account?.let { QRGenerator.generateVietQRBitmap(it, orderId, amount) } 
            }
            
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR",
                    modifier = Modifier.size(220.dp).padding(8.dp).clip(RoundedCornerShape(8.dp))
                )
            }

            Text("Nội dung chuyển khoản:", fontSize = 12.sp)
            Text(orderId, fontWeight = FontWeight.ExtraBold, color = Primary, fontSize = 20.sp)
            Text("Số tiền: ${amount.formatVND()}", fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSimulateSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Simulate Success (Admin)", color = Color.Black, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("(Hệ thống sẽ tự động xác nhận sau khi nhận được tiền)", fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PaymentSuccessContent(amount: Long, orderId: String, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(120.dp), tint = Primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tuyệt vời!", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Primary)
        Text("Đơn hàng của bạn đã được tiếp nhận", textAlign = TextAlign.Center)
        Text("Mã đơn: $orderId", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        Text("Tổng thanh toán: ${amount.formatVND()}", modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        
        Button(
            onClick = onDone, 
            modifier = Modifier.padding(top = 48.dp).fillMaxWidth().height(56.dp), 
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Theo dõi đơn hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
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
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Primary)
        )
        Text(icon, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
        Text(title, fontSize = 15.sp, modifier = Modifier.weight(1f))
    }
}

private fun Long.formatVND(): String = 
    java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(this) + " đ"
