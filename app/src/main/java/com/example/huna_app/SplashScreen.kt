package com.example.huna_app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(key1 = true) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        delay(3000)
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "HUNA",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1960AB),
            modifier = Modifier
                .padding(16.dp)
                .alpha(0.8f)
        )
    }
}
