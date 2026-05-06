package com.example.foodle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodle.navigation.Screen
import com.example.foodle.ui.CartViewModel
import com.example.foodle.ui.MenuViewModel
import com.example.foodle.ui.OrderViewModel
import com.example.foodle.ui.UserViewModel
import com.example.foodle.ui.components.BottomNavigationBar
import com.example.foodle.ui.screens.*
import com.example.foodle.ui.theme.FoodleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodleTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    
    // Khởi tạo UserViewModel sử dụng Factory chuẩn để tránh lỗi import
    val userViewModel: UserViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(context.applicationContext as android.app.Application) as T
            }
        }
    )
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.RoleSelection.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController, userViewModel) }
            composable(Screen.Register.route) { RegisterScreen(navController, userViewModel) }
            composable(Screen.Home.route) { HomeScreen(navController, menuViewModel) }
            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                RestaurantDetailScreen(navController, restaurantId, cartViewModel, menuViewModel)
            }
            composable(Screen.Cart.route) { CartScreen(navController, cartViewModel) }
            composable(Screen.Checkout.route) { CheckoutScreen(navController, cartViewModel, orderViewModel, userViewModel) }
            composable(Screen.DriverSelection.route) { DriverSelectionScreen(navController, orderViewModel) }
            composable(Screen.OrderTracking.route) { OrderTrackingScreen(navController, orderViewModel) }
            composable(Screen.OrdersHistory.route) { OrdersHistoryScreen(navController, orderViewModel) }
            composable(Screen.Profile.route) { ProfileScreen(navController, userViewModel) }
            composable(Screen.RestaurantAdmin.route) { RestaurantAdminScreen(navController, menuViewModel) }
            composable(Screen.DriverApp.route) { DriverAppScreen(navController, orderViewModel) }
        }
    }
}
