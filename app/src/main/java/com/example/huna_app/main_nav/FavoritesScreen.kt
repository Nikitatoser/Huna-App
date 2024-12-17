package com.example.huna_app.main_nav

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.huna_app.Favorite
import com.example.huna_app.Product
import com.example.huna_app.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FavoritesScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    // Стан для збереження списку обраних товарів
    val favoriteProducts = remember { mutableStateOf<List<Product>>(emptyList()) }

    // Завантаження обраних товарів
    LaunchedEffect(userId) {
        // Отримуємо список ID обраних товарів
        db.collection("favorites").document(userId).get()
            .addOnSuccessListener { document ->
                val favorite = document.toObject(Favorite::class.java)
                val productIds = favorite?.productLinks ?: emptyList()

                // Якщо є обрані товари, завантажуємо їх деталі
                if (productIds.isNotEmpty()) {
                    db.collection("products")
                        .whereIn("id", productIds)
                        .get()
                        .addOnSuccessListener { productDocuments ->
                            val products = productDocuments.toObjects(Product::class.java)
                            favoriteProducts.value = products
                        }
                        .addOnFailureListener { e ->
                            Log.e("FavoriteProductsScreen", "Error getting products: $e")
                        }
                } else {
                    favoriteProducts.value = emptyList() // Якщо немає обраних товарів
                }
            }
            .addOnFailureListener { e ->
                Log.e("FavoriteProductsScreen", "Error getting favorites: $e")
            }
    }

    // Виведення списку обраних товарів
    if (favoriteProducts.value.isNotEmpty()) {
        LazyColumn {
            items(favoriteProducts.value) { product ->
                ProductItem(product, navController)
            }
        }
    } else {
        // Показуємо повідомлення, якщо немає обраних товарів
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("У вас немає обраних товарів.")
        }
    }
}