package com.foddy.app.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.foddy.app.R
import com.foddy.app.presentation.navigation.Screen
import com.foddy.app.presentation.ui.theme.OrangeGradient
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(OrangeGradient)),
        contentAlignment = Alignment.Center
    ) {
        // Logo would go here. Using a placeholder for now.
        // Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(150.dp))
    }
}
