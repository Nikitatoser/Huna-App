package com.example.huna_app.ui.theme

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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
                    popUpTo("main") { inclusive = false }  // Переконайтеся, що route збігається з маршрутом у NavHost
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            Modifier.padding(innerPadding)
        ) {
            composable("main") { MainScreen() }
            composable("notifications") { NotificationsScreen() }
            composable("add") { AddScreen() }
            composable("favorites") { FavoritesScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
fun AddScreen() {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Продаю") }
    var price by remember { mutableStateOf("") }
    var deliveryType by remember { mutableStateOf("Самовивіз") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Поле для завантаження фото
        Text(text = "Завантажити фото:")
        UploadPhotoButton(photoUri) { newUri -> photoUri = newUri }

        // Поле для назви товару
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Назва") },
            modifier = Modifier.fillMaxWidth()
        )

        // Поле для опису товару
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Опис") },
            modifier = Modifier.fillMaxWidth()
        )

        // Вибір категорії: "Шукаю" або "Продаю"
        Text(text = "Категорія:")
        Row {
            RadioButton(
                selected = category == "Продаю",
                onClick = { category = "Продаю" }
            )
            Text(text = "Продаю", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = category == "Шукаю",
                onClick = { category = "Шукаю" }
            )
            Text(text = "Шукаю", modifier = Modifier.padding(start = 8.dp))
        }

        // Поле для ціни
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Ціна") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Вибір типу доставки
        Text(text = "Тип доставки:")
        DropdownMenuField(
            selectedOption = deliveryType,
            options = listOf("Самовивіз", "Доставка"),
            onOptionSelected = { deliveryType = it }
        )

        // Кнопка для підтвердження
        Button(
            onClick = { /* Додати логіку для збереження товару */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати товар")
        }
    }
}

@Composable
fun UploadPhotoButton(photoUri: Uri?, onPhotoSelected: (Uri?) -> Unit) {
    Button(onClick = {
        // Логіка для вибору фото
    }) {
        Text(if (photoUri != null) "Фото завантажено" else "Вибрати фото")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Тип доставки") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen() {
    Text(text = "Ви на головній сторінці")
}

@Composable
fun NotificationsScreen() {
    Text(text = "Це сторінка сповіщень")
}


@Composable
fun FavoritesScreen() {
    Text(text = "Це обране")
}


@Composable
fun ProfileScreen() {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        UserProfile(user)
    } else {
        Text("Користувач не авторизований", color = Color.Red, fontSize = 18.sp)
    }
}

@Composable
fun UserProfile(user: FirebaseUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Text(text = user.displayName ?: "Ім'я не вказане", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Email: ${user.email ?: "Не вказано"}", fontSize = 16.sp, color = Color.Gray)
    }
}






@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Головна") },
            label = { Text("Головна") },
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Сповіщення") },
            label = { Text("Сповіщення") },
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Додати") },
            label = { Text("Додати") },
            selected = selectedItem == 2,
            onClick = { onItemSelected(2) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Обране") },
            label = { Text("Обране") },
            selected = selectedItem == 3,
            onClick = { onItemSelected(3) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Профіль") },
            label = { Text("Профіль") },
            selected = selectedItem == 4,
            onClick = { onItemSelected(4) }
        )
    }
}

