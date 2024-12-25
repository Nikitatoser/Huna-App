package com.example.huna_app.main_nav

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.huna_app.Favorite
import com.example.huna_app.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun FavoritesScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    // Стан для обраних товарів, завантаження та помилок
    val favoriteProducts = remember { mutableStateOf<List<Product>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Завантаження обраних товарів із динамічним оновленням
    LaunchedEffect(userId) {
        try {
            db.collection("favorites").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorMessage.value = "Помилка завантаження обраних товарів: ${error.message}"
                        isLoading.value = false
                        return@addSnapshotListener
                    }

                    val favorite = snapshot?.toObject(Favorite::class.java)
                    val productIds = favorite?.productLinks.orEmpty()

                    if (productIds.isNotEmpty()) {
                        db.collection("products")
                            .whereIn("id", productIds)
                            .addSnapshotListener { productSnapshots, productError ->
                                if (productError != null) {
                                    errorMessage.value = "Помилка завантаження товарів: ${productError.message}"
                                    isLoading.value = false
                                    return@addSnapshotListener
                                }

                                val products = productSnapshots?.toObjects(Product::class.java).orEmpty()
                                favoriteProducts.value = products
                                isLoading.value = false
                            }
                    } else {
                        favoriteProducts.value = emptyList()
                        isLoading.value = false
                    }
                }
        } catch (e: Exception) {
            errorMessage.value = "Помилка завантаження обраних товарів: ${e.message}"
            isLoading.value = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Верхній заголовок із кнопкою "Назад"
        TopBar(navController)

        // Основний контент
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage.value != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage.value.orEmpty(),
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }

            favoriteProducts.value.isNotEmpty() -> {
                LazyColumn {
                    items(favoriteProducts.value) { product ->
                        ProductItem(product = product, navController = navController)
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("У вас немає обраних товарів.")
                }
            }
        }
    }
}

@Composable
fun TopBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1960AB))
                .clickable { navController.navigateUp() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Favourites",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1960AB)
        )
    }
}



@Composable
fun ProductItem(product: Product, navController: NavHostController) {
    val sellerName = remember { mutableStateOf("Завантаження...") }
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val isRemoving = remember { mutableStateOf(false) }

    // Завантаження імені продавця
    LaunchedEffect(product.ownerId) {
        try {
            val document = db.collection("users").document(product.ownerId).get().await()
            sellerName.value = document.getString("name") ?: "Невідомий продавець"
        } catch (e: Exception) {
            sellerName.value = "Помилка завантаження"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("product_details/${product.id}") }, // Перехід на сторінку деталей товару
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Зображення товару
            val imageModifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)

            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Зображення товару",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = imageModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "No Image",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Інформація про товар
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Продавець: ${sellerName.value}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Ціна
                Box(
                    modifier = Modifier
                        .background(Color(0xFF90B9F6), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${product.price} $",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(   if (isRemoving.value) Color.LightGray else Color(0xFF1960AB))
                    .clickable {
                        isRemoving.value = true
                        db.collection("favorites").document(userId)
                            .update("productLinks", FieldValue.arrayRemove(product.id))
                            .addOnSuccessListener {
                                isRemoving.value = false
                            }
                            .addOnFailureListener { e ->
                                isRemoving.value = false
                                Log.e("ProductItem", "Error removing product from favorites: ${e.message}")
                            }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRemoving.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color.White
                )
            }

        }
    }
}


