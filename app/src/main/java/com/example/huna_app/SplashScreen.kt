package com.example.huna_app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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
    // Використовуємо LaunchedEffect для затримки і перевірки авторизації
    LaunchedEffect(key1 = true) {
        // Отримуємо поточного користувача з FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Затримка на 3 секунди (можна прибрати або скоротити)
        delay(3000)

        // Перевіряємо чи є користувач авторизованим
        if (currentUser != null) {
            // Якщо користувач авторизований, переходимо на головний екран
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true } // Видаляємо сплеш-екран з навігації
            }
        } else {
            // Якщо користувач не авторизований, переходимо на екран входу
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true } // Видаляємо сплеш-екран з навігації
            }
        }
    }

    // Відображення контенту сплеш-скріну
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Відображення тексту "HUNA" по центру екрану
        Text(
            text = "HUNA",
            fontSize = 80.sp, // Великий розмір шрифта
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1960AB), // Синій колір тексту
            modifier = Modifier
                .padding(16.dp)
                .alpha(0.8f) // Легкий ефект прозорості
        )
    }
}
