package com.example.huna_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huna_app.main_nav.UsersItems
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            Navigation()
        }
    }
}

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "HUNA", fontSize = 50.sp, color = Color(0xFFF88837),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}



@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}