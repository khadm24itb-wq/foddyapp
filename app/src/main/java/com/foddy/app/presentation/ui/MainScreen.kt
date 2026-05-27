package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.components.BottomNavigationBar
import com.foddy.app.presentation.viewmodel.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val userViewModel: UserViewModel = hiltViewModel()
    val menuViewModel: MenuViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val restaurantViewModel: RestaurantViewModel = hiltViewModel()
    val recommendationViewModel: RecommendationViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.OrdersHistory.route,
        Screen.Profile.route,
        Screen.AIAssistant.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController, userViewModel) }
            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController, userViewModel) }
            composable(Screen.Register.route) { RegisterScreen(navController, userViewModel) }
            composable(Screen.Home.route) { 
                HomeScreen(navController, menuViewModel, recommendationViewModel, restaurantViewModel, cartViewModel, userViewModel) 
            }
            composable(Screen.Notifications.route) {
                NotificationScreen(navController, notificationViewModel)
            }
            composable(Screen.RestaurantDetail.route) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                RestaurantDetailScreen(navController, restaurantId, cartViewModel, menuViewModel, restaurantViewModel)
            }
            composable(Screen.Cart.route) { CartScreen(navController, cartViewModel) }
            composable(Screen.Checkout.route) { CheckoutScreen(navController, userViewModel) }
            composable(Screen.OrderTracking.route) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(navController, orderId, orderViewModel)
            }
            composable(Screen.OrdersHistory.route) { OrdersHistoryScreen(navController, orderViewModel) }
            composable(Screen.Profile.route) { ProfileScreen(navController, userViewModel) }
            composable(Screen.AIAssistant.route) { AIAssistantScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            
            // Roles
            composable(Screen.RestaurantAdmin.route) { RestaurantAdminScreen(navController, menuViewModel, orderViewModel, userViewModel) }
            composable(Screen.DriverApp.route) { DriverAppScreen(navController, orderViewModel) }
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
        }
    }
}
