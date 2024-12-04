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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.huna_app.Product
import com.example.huna_app.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

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
        // Виведення інформації про користувача
        currentUser?.let { user ->
            Text(
                text = "Ім'я користувача: ${user.displayName ?: "Немає"}",

            )
            Spacer(modifier = Modifier.height(8.dp))
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
                auth.signOut()
                navController.navigate("login_screen") { // Перехід на екран логіну
                    popUpTo("profile") { inclusive = true }
                }
            }
        ) {
            Text(text = "Вийти з акаунта")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("user_items")
            }
        ) {
            Text(text = "ВАШІ ТОВАРИ")
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
fun UserProfile(user: FirebaseUser, db: FirebaseFirestore, navController: NavHostController) {
    // Список товарів
    val productsList = remember { mutableStateListOf<Product>() }

    // Запит на отримання товарів для поточного користувача
    LaunchedEffect(user) {
        db.collection("products")
            .whereEqualTo("ownerId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                productsList.clear()  // Очищаємо список перед заповненням новими даними
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productsList.add(product)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching products for user", exception)
            }
    }

    // Відображення інтерфейсу користувача
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Виведення даних користувача
        Text(text = user.displayName ?: "Ім'я не вказане", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Email: ${user.email ?: "Не вказано"}", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ваші товари:", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Список товарів
        if (productsList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(productsList) { product ->
                    ProductItem(
                        product = product,
                        navController = navController // Передаємо навігатор для переходу
                    )
                }
            }
        } else {
            // Повідомлення, якщо немає товарів
            Text("У вас немає товарів", fontSize = 16.sp, color = Color.Gray)
        }
    }
}


private fun signOut(auth: FirebaseAuth){
    auth.signOut()
}



fun getProductsForUser(db: FirebaseFirestore, user: FirebaseUser, callback: (List<Product>) -> Unit) {
    try {
        db.collection("products")
            .whereEqualTo("ownerId", user.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = mutableListOf<Product>()
                for (document in querySnapshot) {
                    val product = document.toObject(Product::class.java)
                    products.add(product)
                }
                callback(products)  // Передаємо правильний список об'єктів
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching products for user", e)
            }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching products", e)
    }
}