package com.example.huna_app


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.huna_app.main_nav.AccountSettingsScreen
import com.example.huna_app.main_nav.AddScreen
import com.example.huna_app.main_nav.AllSettingsScreen
import com.example.huna_app.main_nav.MainScreen
import com.example.huna_app.main_nav.FavoritesScreen
import com.example.huna_app.main_nav.ProfileScreen
import com.example.huna_app.main_nav.UsersItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavController) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }
    val routesWithBottomBar = listOf("main", "notifications", "add", "favorites", "profile")
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentDestination in routesWithBottomBar) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            Modifier.padding(innerPadding)
        ) {
            composable("main") { MainScreen(db = FirebaseFirestore.getInstance(), navController = navController) }
            composable("notifications") { AllChatScreen(navController) }
            composable("add") { AddScreen(navController) }
            composable("favorites") { FavoritesScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("user_items") { UsersItems(navController) }
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("account_settings") { AccountSettingsScreen(navController) }
            composable("all_settings") { AllSettingsScreen(navController) }
            composable("product_details/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(productId, navController)
            }
            composable("chat/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""

                ChatScreen(
                    chatId = chatId,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .background(Color(0xFF1960AB))

    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Головна",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = selectedItem == 0,
                onClick = { onItemSelected(0) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF1960AB)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Сповіщення",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = selectedItem == 1,
                onClick = { onItemSelected(1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF1960AB)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Додати",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = selectedItem == 2,
                onClick = { onItemSelected(2) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF1960AB)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Обране",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = selectedItem == 3,
                onClick = { onItemSelected(3) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF1960AB)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Профіль",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = selectedItem == 4,
                onClick = { onItemSelected(4) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF1960AB)
                )
            )
        }
    }
}





