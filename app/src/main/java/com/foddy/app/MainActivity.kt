package com.foddy.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.viewmodel.*
import com.foddy.app.presentation.ui.*
import com.foddy.app.presentation.ui.components.BottomNavigationBar
import com.foddy.app.presentation.ui.theme.FoddyAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoddyAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val menuViewModel: MenuViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()
    val recommendationViewModel: RecommendationViewModel = hiltViewModel()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController, userViewModel) }
            composable(Screen.Register.route) { RegisterScreen(navController, userViewModel) }
            composable(Screen.Home.route) { HomeScreen(navController, menuViewModel) }
            
            // New optimized feature screen
            composable("posts") { 
                MainScreen(viewModel = mainViewModel, onPostClick = {}) 
            }

            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                RestaurantDetailScreen(navController, restaurantId, cartViewModel, menuViewModel)
            }
            composable(Screen.Cart.route) { CartScreen(navController, cartViewModel) }
            composable(Screen.Checkout.route) { CheckoutScreen(navController, cartViewModel, userViewModel) }
            composable(Screen.DriverSelection.route) { DriverSelectionScreen(navController, orderViewModel) }
            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(navController, orderId, orderViewModel)
            }
            composable(Screen.OrdersHistory.route) { OrdersHistoryScreen(navController, orderViewModel) }
            composable(Screen.Profile.route) { ProfileScreen(navController, userViewModel) }
            composable(Screen.AIAssistant.route) { AIAssistantScreen(navController, recommendationViewModel, menuViewModel) }
            composable(Screen.RestaurantAdmin.route) { RestaurantAdminScreen(navController, menuViewModel) }
            composable(Screen.DriverApp.route) { DriverAppScreen(navController, orderViewModel) }
        }
    }
}
