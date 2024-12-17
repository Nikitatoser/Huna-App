package com.example.huna_app

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huna_app.main_nav.AccountSettingsScreen
import com.example.huna_app.main_nav.AddScreen
import com.example.huna_app.main_nav.AllSettingsScreen
import com.example.huna_app.main_nav.MainScreen
import com.example.huna_app.main_nav.FavoritesScreen
import com.example.huna_app.main_nav.ProfileScreen
import com.example.huna_app.main_nav.UsersItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController) {
    val navController = rememberNavController()  // Зберігаємо NavController тут

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Головна", "Сповіщення", "Додати", "Обране", "Профіль")

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedItem) { selectedIndex ->
                selectedItem = selectedIndex
                val route = when (selectedIndex) {
                    0 -> "main"
                    1 -> "notifications"
                    2 -> "add"
                    3 -> "favorites"
                    else -> "profile"
                }
                navController.navigate(route) {
                    popUpTo("main") { inclusive = false }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            Modifier.padding(innerPadding)
        ) {
            composable("main") { MainScreen(db = FirebaseFirestore.getInstance(), navController = navController) }
            composable("notifications") { NotificationsScreen() }
            composable("add") { AddScreen() }
            composable("favorites") { FavoritesScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("user_items") { UsersItems(navController) }

            composable("account_settings") { AccountSettingsScreen(navController) }
            composable("all_settings") { AllSettingsScreen(navController) }
            // Динамічний маршрут для деталей товару
            composable("product_details/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(productId, navController)
            }
        }
    }
}


@Composable
fun NotificationsScreen() {
    Text(text = "Це сторінка сповіщень")
}


@Composable
fun ProductItem(product: Product, navController: NavHostController) {
    // Стан для збереження імені продавця
    var sellerName by remember { mutableStateOf("Завантаження...") }

    // Завантаження імені з Firestore
    LaunchedEffect(product.ownerId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val document = db.collection("users").document(product.ownerId).get().await()
            sellerName = document.getString("name") ?: "Невідомий продавець"
        } catch (e: Exception) {
            sellerName = "Помилка завантаження"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("product_details/${product.id}") // Переходимо на сторінку деталей товару
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(text = product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "${product.price} $", fontSize = 16.sp)
                Text(text = sellerName, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}







@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Головна") },
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Сповіщення") },
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Додати") },
            selected = selectedItem == 2,
            onClick = { onItemSelected(2) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Обране") },
            selected = selectedItem == 3,
            onClick = { onItemSelected(3) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Профіль") },
            selected = selectedItem == 4,
            onClick = { onItemSelected(4) }
        )
    }
}



