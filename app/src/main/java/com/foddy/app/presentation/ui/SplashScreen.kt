package com.foddy.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.OrangeGradient
import com.foddy.app.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, userViewModel: UserViewModel) {
    val uiState by userViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            delay(1000) // Small delay for branding
            val user = uiState.user
            if (user != null && user.isLoggedIn) {
                // Navigate based on role
                when (user.role) {
                    "DRIVER" -> navController.navigate(Screen.DriverApp.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    "RESTAURANT" -> navController.navigate(Screen.RestaurantAdmin.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    "ADMIN" -> navController.navigate(Screen.RestaurantAdmin.route) { // Placeholder for ADMIN
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    else -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(OrangeGradient)),
        contentAlignment = Alignment.Center
    ) {
        // Logo or branding here
    }
}
