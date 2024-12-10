package com.example.huna_app.main_nav

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.huna_app.Product
import com.example.huna_app.ProductItem
import com.example.huna_app.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase




@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser





    // Додамо Column для розташування елементів
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UserProfile(user = currentUser!!, db = FirebaseFirestore.getInstance(), navController = navController)
        // Виведення інформації про користувача
        currentUser?.let { user ->
            Text(
                text = "Електронна пошта: ${user.email ?: "Немає"}",

            )
        } ?: run {
            // Якщо користувач не авторизований
            Text(
                text = "Користувач не авторизований",

            )
        }



        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка для виходу з акаунта
        Button(
            onClick = {
                navController.navigate("account_settings")
            }
        ) {
            Text(text = "Account")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("user_items")
            }
        ) {
            Text(text = "Your items")
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка для виходу з акаунта
        Button(
            onClick = {
                navController.navigate("all_settings")
            }
        ) {
            Text(text = "Settings")
        }
    }
}


@Composable
fun UsersItems(navController: NavHostController){
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    val products = remember { mutableStateListOf<Product>() }

    // Завантаження товарів
    LaunchedEffect(user) {
        user?.let {
            db.collection("products")
                .whereEqualTo("ownerId", it.uid)
                .get()
                .addOnSuccessListener { result ->
                    products.clear()
                    for (document in result) {
                        val product = document.toObject(Product::class.java)
                        products.add(product)
                    }
                }
        }
    }

    if (products.isNotEmpty()) {
        LazyColumn {
            items(products) { product ->
                ProductItem(product = product, navController = navController)
            }
        }
    } else {
        Text("У вас немає товарів", fontSize = 16.sp, color = Color.Gray)
    }
}





@Composable
fun AccountSettingsScreen(navController: NavHostController){
    val auth = FirebaseAuth.getInstance()


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = "Account Settings")
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("home") { // Перехід на екран логіну
                    popUpTo("profile") { inclusive = true }
                }
            }
        ) {
            Text(text = "Log out")
        }

        Spacer(modifier = Modifier.height(16.dp))

        var showDialog by remember { mutableStateOf(false) }
        val userId = auth.currentUser?.uid

        Button(onClick = { showDialog = true }) {
            Text(text = "Delete account")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Are you sure you want to delete your account?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        // Викликаємо функцію видалення акаунта та його даних
                        if (userId != null) {
                            deleteAccount(auth, userId, navController)
                        }
                        showDialog = false
                    }) {
                        Text("Yes, Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}

fun deleteAccount(auth: FirebaseAuth, userId: String, navController: NavController) {
    // Видалити всі товари користувача
    deleteUserProducts(userId) { isProductDeleted ->
        if (isProductDeleted) {
            // Товари успішно видалені, тепер видаляємо дані користувача з Firestore
            deleteUserDataFromFirestore(userId) { isUserDataDeleted ->
                if (isUserDataDeleted) {
                    // Якщо дані користувача також видалені, видаляємо акаунт
                    auth.currentUser?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.signOut()
                                navController.navigate("home") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            } else {
                                Log.e("DeleteAccount", "Failed to delete account: ${task.exception}")
                            }
                        }
                } else {
                    Log.e("DeleteAccount", "Failed to delete user data from Firestore")
                }
            }
        } else {
            Log.e("DeleteAccount", "Failed to delete user products")
        }
    }
}

fun deleteUserProducts(userId: String, onComplete: (Boolean) -> Unit) {
    val productsCollection = FirebaseFirestore.getInstance().collection("products")

    productsCollection.whereEqualTo("ownerId", userId)
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val products = task.result
                if (products != null && !products.isEmpty) {
                    for (product in products) {
                        product.reference.delete()
                            .addOnSuccessListener {
                                if (product == products.last()) {
                                    onComplete(true)
                                }
                            }
                            .addOnFailureListener {
                                Log.e("DeleteAccount", "Failed to delete product: ${product.id}")
                                onComplete(false)
                            }
                    }
                } else {
                    onComplete(true)
                }
            } else {
                Log.e("DeleteAccount", "Failed to fetch products: ${task.exception}")
                onComplete(false)
            }
        }
}

fun deleteUserDataFromFirestore(userId: String, onComplete: (Boolean) -> Unit) {
    val usersCollection = FirebaseFirestore.getInstance().collection("users")

    usersCollection.document(userId).delete()
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener { exception ->
            Log.e("DeleteAccount", "Error deleting user data: ${exception.localizedMessage}")
            onComplete(false)
        }
}





@Composable
fun AllSettingsScreen(navController: NavHostController){
    Text(text = "All Settings")
}






@Composable
fun UserProfile(user: FirebaseUser, db: FirebaseFirestore, navController: NavHostController) {
    // Створення стану для даних користувача
    val userData = remember { mutableStateOf<User?>(null) }

    // Запит на отримання даних користувача
    LaunchedEffect(user) {
        // Отримуємо дані користувача з колекції "users" за ID користувача (user.uid)
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fetchedUser = document.toObject(User::class.java)
                    userData.value = fetchedUser
                } else {
                    Log.e("Firestore", "User document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching user data", exception)
            }
    }

    // Відображення інтерфейсу користувача
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Виведення даних користувача, якщо вони доступні
        userData.value?.let { fetchedUser ->
            Text(text = "Ім'я: ${fetchedUser.name ?: "Невідомо"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Вік: ${fetchedUser.age ?: "Невідомо"}", fontSize = 18.sp)
            Text(text = "Адреса: ${fetchedUser.address ?: "Невідомо"}", fontSize = 18.sp)
        } ?: run {
            // Якщо дані користувача не знайдено або вони не завантажені
            Text(text = "Не вдалося завантажити дані користувача", fontSize = 16.sp, color = Color.Red)
        }
    }
}

